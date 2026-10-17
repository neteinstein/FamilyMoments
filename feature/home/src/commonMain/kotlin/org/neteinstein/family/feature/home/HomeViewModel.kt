package org.neteinstein.family.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.neteinstein.family.domain.analytics.AnalyticsEvents
import org.neteinstein.family.domain.analytics.AnalyticsParams
import org.neteinstein.family.domain.analytics.AnalyticsScreens
import org.neteinstein.family.domain.analytics.AnalyticsTracker
import org.neteinstein.family.domain.analytics.AnalyticsUserProperties
import org.neteinstein.family.domain.analytics.analyticsName
import org.neteinstein.family.domain.analytics.bucketHiddenCards
import org.neteinstein.family.domain.model.Question
import org.neteinstein.family.domain.model.QuestionCategory
import org.neteinstein.family.domain.usecase.GetContentLanguageUseCase
import org.neteinstein.family.domain.usecase.GetQuestionsUseCase
import org.neteinstein.family.domain.usecase.GetUsedQuestionIdsUseCase
import org.neteinstein.family.domain.usecase.MarkQuestionUsedUseCase

data class HomeUiState(
    val currentQuestion: Question? = null,
    val isLoading: Boolean = true,
    val currentIndex: Int = 0,
    val totalQuestions: Int = 0,
    val selectedCategory: QuestionCategory? = null,
    val questions: List<Question> = emptyList(),
)

class HomeViewModel(
    private val getQuestionsUseCase: GetQuestionsUseCase,
    private val getContentLanguageUseCase: GetContentLanguageUseCase,
    private val getUsedQuestionIdsUseCase: GetUsedQuestionIdsUseCase,
    private val markQuestionUsedUseCase: MarkQuestionUsedUseCase,
    private val analyticsTracker: AnalyticsTracker,
) : ViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private var allQuestions: List<Question> = emptyList()
    private var questions: List<Question> = emptyList()
    private var usedQuestionIds: Set<Int> = emptySet()
    private var loadedLanguageCode: String? = null

    init {
        loadQuestions()
    }

    /**
     * Re-checks the active content language ([GetContentLanguageUseCase] - the OS/browser locale,
     * or the user's in-app override) and reloads questions if it changed since the last load - the
     * user can change it via Settings without this ViewModel (scoped to the Home back stack entry)
     * being recreated, so [init] alone isn't enough to pick that up. Otherwise, just refreshes
     * which cards are hidden, so returning from Settings after a "Reset Cards" makes previously
     * hidden cards reappear without needing a full reload.
     */
    fun onScreenEntered() {
        val languageCode = getContentLanguageUseCase()
        if (languageCode != loadedLanguageCode) {
            loadQuestions(languageCode)
        } else {
            refreshUsedQuestions()
        }
    }

    private fun refreshUsedQuestions() {
        viewModelScope.launch {
            usedQuestionIds = getUsedQuestionIdsUseCase()
            applyFilter(_uiState.value.selectedCategory)
        }
    }

    fun loadQuestions(languageCode: String = getContentLanguageUseCase()) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            loadedLanguageCode = languageCode
            allQuestions = getQuestionsUseCase(languageCode)
            usedQuestionIds = getUsedQuestionIdsUseCase()
            applyFilter(_uiState.value.selectedCategory)
            // The first card of a freshly loaded deck is on screen without any navigation having
            // happened, so nothing else would report it.
            logCurrentQuestionViewed(source = SOURCE_DECK)
        }
    }

    fun onCategorySelected(category: QuestionCategory?) {
        analyticsTracker.logEvent(
            AnalyticsEvents.CATEGORY_SELECTED,
            mapOf(AnalyticsParams.CATEGORY to category.analyticsName()),
        )
        applyFilter(category)
        logCurrentQuestionViewed(source = SOURCE_DECK)
    }

    fun markCurrentQuestionAsUsed() {
        val question = _uiState.value.currentQuestion ?: return
        viewModelScope.launch {
            markQuestionUsedUseCase(question.id)
            usedQuestionIds = usedQuestionIds + question.id
            // Grouped by question_id in the console, this is the app's best content-quality
            // signal: the questions people hide most are the ones worth rewriting or dropping
            // from QuestionSeedData.
            analyticsTracker.logEvent(
                AnalyticsEvents.QUESTION_HIDDEN,
                mapOf(
                    AnalyticsParams.QUESTION_ID to question.id,
                    AnalyticsParams.CATEGORY to question.category.analyticsName(),
                    AnalyticsParams.HIDDEN_TOTAL to usedQuestionIds.size,
                ),
            )
            analyticsTracker.setUserProperty(
                AnalyticsUserProperties.HIDDEN_CARDS_BUCKET,
                bucketHiddenCards(usedQuestionIds.size),
            )
            applyFilter(_uiState.value.selectedCategory)
            logCurrentQuestionViewed(source = SOURCE_DECK)
        }
    }

    private fun applyFilter(category: QuestionCategory?) {
        questions =
            allQuestions
                .filter { (category == null || it.category == category) && it.id !in usedQuestionIds }
                .shuffled()
        val firstQuestion = questions.firstOrNull()
        _uiState.update {
            it.copy(
                isLoading = false,
                currentQuestion = firstQuestion,
                currentIndex = 0,
                totalQuestions = questions.size,
                selectedCategory = category,
                questions = questions,
            )
        }
    }

    /**
     * [input] distinguishes a swipe from the arrow-key shortcuts (effectively Web-only) - a big
     * gap between the two on Web would say the swipe affordance isn't discoverable with a mouse.
     */
    fun nextQuestion(input: String = INPUT_SWIPE) {
        if (questions.isEmpty()) return
        val nextIndex = (_uiState.value.currentIndex + 1) % questions.size
        _uiState.update {
            it.copy(
                currentQuestion = questions[nextIndex],
                currentIndex = nextIndex,
            )
        }
        logDeckNavigated(direction = DIRECTION_NEXT, input = input)
        logCurrentQuestionViewed(source = SOURCE_DECK)
    }

    fun previousQuestion(input: String = INPUT_SWIPE) {
        if (questions.isEmpty()) return
        val prevIndex = (_uiState.value.currentIndex - 1 + questions.size) % questions.size
        _uiState.update {
            it.copy(
                currentQuestion = questions[prevIndex],
                currentIndex = prevIndex,
            )
        }
        logDeckNavigated(direction = DIRECTION_PREVIOUS, input = input)
        logCurrentQuestionViewed(source = SOURCE_DECK)
    }

    /**
     * The shuffle button, the grid/swipe toggle and opening a card full screen are pure UI state
     * in [HomeScreen] with no ViewModel involvement at all. They are routed through these
     * no-state methods anyway, rather than calling the tracker from the composable, so they stay
     * unit-testable alongside the rest of this class and so HomeScreen keeps no logic of its own -
     * see AGENTS.md's MVVM conventions.
     */
    fun onShuffleClicked() {
        analyticsTracker.logEvent(
            AnalyticsEvents.SHUFFLE_USED,
            mapOf(
                AnalyticsParams.CATEGORY to _uiState.value.selectedCategory.analyticsName(),
                AnalyticsParams.DECK_SIZE to questions.size,
            ),
        )
    }

    /** [mode] is [MODE_GRID] or [MODE_SWIPE] - the state the toggle just moved *to*. */
    fun onViewModeToggled(mode: String) {
        analyticsTracker.logEvent(AnalyticsEvents.VIEW_MODE_CHANGED, mapOf(AnalyticsParams.MODE to mode))
        analyticsTracker.logScreenView(
            if (mode == MODE_GRID) AnalyticsScreens.HOME_GRID else AnalyticsScreens.HOME,
        )
    }

    /** [source] is [SOURCE_SWIPE_UP], [SOURCE_GRID], [SOURCE_SHUFFLE] or [SOURCE_FULLSCREEN_RANDOM]. */
    fun onQuestionExpanded(
        question: Question,
        source: String,
    ) {
        analyticsTracker.logEvent(AnalyticsEvents.QUESTION_EXPANDED, mapOf(AnalyticsParams.SOURCE to source))
        analyticsTracker.logScreenView(AnalyticsScreens.QUESTION_FULLSCREEN)
        logQuestionViewed(question, source)
    }

    /** The full-screen card was closed, so the deck underneath is showing again. */
    fun onFullScreenClosed() {
        analyticsTracker.logScreenView(AnalyticsScreens.HOME)
    }

    private fun logDeckNavigated(
        direction: String,
        input: String,
    ) {
        analyticsTracker.logEvent(
            AnalyticsEvents.DECK_NAVIGATED,
            mapOf(
                AnalyticsParams.DIRECTION to direction,
                AnalyticsParams.INPUT to input,
            ),
        )
    }

    private fun logCurrentQuestionViewed(source: String) {
        _uiState.value.currentQuestion?.let { logQuestionViewed(it, source) }
    }

    private fun logQuestionViewed(
        question: Question,
        source: String,
    ) {
        analyticsTracker.logEvent(
            AnalyticsEvents.QUESTION_VIEWED,
            mapOf(
                AnalyticsParams.QUESTION_ID to question.id,
                AnalyticsParams.CATEGORY to question.category.analyticsName(),
                AnalyticsParams.LANGUAGE to (loadedLanguageCode ?: ""),
                AnalyticsParams.DECK_POSITION to _uiState.value.currentIndex,
                AnalyticsParams.SOURCE to source,
            ),
        )
    }

    companion object {
        const val DIRECTION_NEXT = "next"
        const val DIRECTION_PREVIOUS = "previous"
        const val INPUT_SWIPE = "swipe"
        const val INPUT_KEYBOARD = "keyboard"
        const val MODE_GRID = "grid"
        const val MODE_SWIPE = "swipe"
        const val SOURCE_DECK = "deck"
        const val SOURCE_GRID = "grid"
        const val SOURCE_SHUFFLE = "shuffle"
        const val SOURCE_SWIPE_UP = "swipe_up"
        const val SOURCE_FULLSCREEN_RANDOM = "fullscreen_random"
    }
}
