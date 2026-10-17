package org.neteinstein.family.feature.home

import androidx.compose.ui.input.key.Key
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.pressKey
import androidx.test.core.app.ApplicationProvider
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.setResourceReaderAndroidContext
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.neteinstein.family.domain.model.Question
import org.neteinstein.family.domain.usecase.GetContentLanguageUseCase
import org.neteinstein.family.domain.usecase.GetQuestionsUseCase
import org.neteinstein.family.domain.usecase.GetUsedQuestionIdsUseCase
import org.neteinstein.family.domain.usecase.MarkQuestionUsedUseCase
import org.neteinstein.family.ui.theme.FamilyMomentsTheme
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], qualifiers = "w411dp-h891dp")
class HomeScreenGridViewTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    // See HomeScreenCategoryDropdownTest's identical setup for why this is needed
    // (https://github.com/robolectric/robolectric/issues/9603).
    @OptIn(ExperimentalResourceApi::class)
    @Before
    fun setUpComposeResourcesContext() {
        setResourceReaderAndroidContext(ApplicationProvider.getApplicationContext())
    }

    private val fakeQuestions =
        listOf(
            Question(id = 1, text = "Grid question one?", languageCode = "en"),
            Question(id = 2, text = "Grid question two?", languageCode = "en"),
            Question(id = 3, text = "Grid question three?", languageCode = "en"),
        )

    private fun buildViewModel(): HomeViewModel {
        val getQuestionsUseCase: GetQuestionsUseCase = mockk()
        val getContentLanguageUseCase: GetContentLanguageUseCase = mockk()
        val getUsedQuestionIdsUseCase: GetUsedQuestionIdsUseCase = mockk()
        val markQuestionUsedUseCase: MarkQuestionUsedUseCase = mockk()
        every { getContentLanguageUseCase() } returns "en"
        coEvery { getQuestionsUseCase(any()) } returns fakeQuestions
        coEvery { getUsedQuestionIdsUseCase() } returns emptySet()
        coEvery { markQuestionUsedUseCase(any()) } returns Unit
        return HomeViewModel(
            getQuestionsUseCase,
            getContentLanguageUseCase,
            getUsedQuestionIdsUseCase,
            markQuestionUsedUseCase,
            mockk(relaxed = true),
        )
    }

    @Test
    fun `switching to grid view shows every question as a card`() {
        composeTestRule.setContent {
            FamilyMomentsTheme(darkTheme = false, dynamicColor = false) {
                HomeScreen(onSettingsClick = {}, viewModel = buildViewModel())
            }
        }

        composeTestRule.onNodeWithContentDescription("Switch to grid view").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Grid question one?").assertExists()
        composeTestRule.onNodeWithText("Grid question two?").assertExists()
        composeTestRule.onNodeWithText("Grid question three?").assertExists()
        composeTestRule.onNodeWithContentDescription("Switch to card view").assertExists()
    }

    @Test
    fun `tapping a grid card opens it full screen`() {
        composeTestRule.setContent {
            FamilyMomentsTheme(darkTheme = false, dynamicColor = false) {
                HomeScreen(onSettingsClick = {}, viewModel = buildViewModel())
            }
        }

        composeTestRule.onNodeWithContentDescription("Switch to grid view").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Grid question two?").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithContentDescription("Close").assertExists()
    }

    @Test
    fun `switching back to card view restores the swipe hints`() {
        composeTestRule.setContent {
            FamilyMomentsTheme(darkTheme = false, dynamicColor = false) {
                HomeScreen(onSettingsClick = {}, viewModel = buildViewModel())
            }
        }

        composeTestRule.onNodeWithContentDescription("Switch to grid view").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithContentDescription("Switch to card view").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithContentDescription("Switch to grid view").assertExists()
    }

    // The grid keeps showing every card, so the expanded card is the second node with its text.
    private fun openGridCardFullScreen(
        viewModel: HomeViewModel,
        index: Int,
    ): List<String> {
        composeTestRule.setContent {
            FamilyMomentsTheme(darkTheme = false, dynamicColor = false) {
                HomeScreen(onSettingsClick = {}, viewModel = viewModel)
            }
        }
        composeTestRule.onNodeWithContentDescription("Switch to grid view").performClick()
        composeTestRule.waitForIdle()
        // The view model shuffles, so read back the order the arrow keys will step through.
        val texts =
            viewModel.uiState.value.questions
                .map { it.text }
        composeTestRule.onNodeWithText(texts[index]).performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onAllNodesWithText(texts[index]).assertCountEquals(2)
        return texts
    }

    private fun pressKey(key: Key) {
        composeTestRule.onRoot().performKeyInput { pressKey(key) }
        composeTestRule.waitForIdle()
    }

    @Test
    fun `arrow down on an expanded card shows the next card and wraps around`() {
        val texts = openGridCardFullScreen(buildViewModel(), index = 0)

        pressKey(Key.DirectionDown)
        composeTestRule.onAllNodesWithText(texts[1]).assertCountEquals(2)
        composeTestRule.onAllNodesWithText(texts[0]).assertCountEquals(1)

        pressKey(Key.DirectionDown)
        pressKey(Key.DirectionDown)
        composeTestRule.onAllNodesWithText(texts[0]).assertCountEquals(2)
    }

    @Test
    fun `arrow up on an expanded card shows the previous card and wraps around`() {
        val texts = openGridCardFullScreen(buildViewModel(), index = 0)

        pressKey(Key.DirectionUp)
        composeTestRule.onAllNodesWithText(texts[2]).assertCountEquals(2)
        composeTestRule.onAllNodesWithText(texts[0]).assertCountEquals(1)

        pressKey(Key.DirectionUp)
        composeTestRule.onAllNodesWithText(texts[1]).assertCountEquals(2)
    }

    @Test
    fun `arrow keys do nothing to the grid while no card is expanded`() {
        composeTestRule.setContent {
            FamilyMomentsTheme(darkTheme = false, dynamicColor = false) {
                HomeScreen(onSettingsClick = {}, viewModel = buildViewModel())
            }
        }
        composeTestRule.onNodeWithContentDescription("Switch to grid view").performClick()
        composeTestRule.waitForIdle()

        pressKey(Key.DirectionDown)

        composeTestRule.onNodeWithContentDescription("Close").assertDoesNotExist()
    }
}
