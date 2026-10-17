package org.neteinstein.family.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.neteinstein.family.domain.analytics.ANALYTICS_VALUE_AUTOMATIC
import org.neteinstein.family.domain.analytics.AnalyticsEvents
import org.neteinstein.family.domain.analytics.AnalyticsParams
import org.neteinstein.family.domain.analytics.AnalyticsTracker
import org.neteinstein.family.domain.model.AppLanguage
import org.neteinstein.family.domain.model.AppUpdate
import org.neteinstein.family.domain.model.ThemeMode
import org.neteinstein.family.domain.model.UpdateCheckResult
import org.neteinstein.family.domain.repository.AppUpdateInstaller
import org.neteinstein.family.domain.usecase.CheckForUpdateUseCase
import org.neteinstein.family.domain.usecase.DownloadAppUpdateUseCase
import org.neteinstein.family.domain.usecase.GetAnalyticsEnabledUseCase
import org.neteinstein.family.domain.usecase.GetContentLanguageUseCase
import org.neteinstein.family.domain.usecase.GetLanguageOverrideUseCase
import org.neteinstein.family.domain.usecase.GetQuestionsUseCase
import org.neteinstein.family.domain.usecase.GetThemeModeUseCase
import org.neteinstein.family.domain.usecase.GetUsedQuestionIdsUseCase
import org.neteinstein.family.domain.usecase.ResetUsedQuestionsUseCase
import org.neteinstein.family.domain.usecase.SetAnalyticsEnabledUseCase
import org.neteinstein.family.domain.usecase.SetLanguageOverrideUseCase
import org.neteinstein.family.domain.usecase.SetThemeModeUseCase

/**
 * Runs a background update check when Settings is entered so the "Update to latest" button can
 * reflect availability immediately (green when a release is found), without auto-downloading.
 *
 * [updatesEnabled] is false on the Play Store flavor, which the Play Store itself updates - the
 * self-update check never runs and SettingsScreen hides the "Updates" section entirely.
 */
class SettingsViewModel(
    private val checkForUpdateUseCase: CheckForUpdateUseCase,
    private val downloadAppUpdateUseCase: DownloadAppUpdateUseCase,
    private val appUpdateInstaller: AppUpdateInstaller,
    private val resetUsedQuestionsUseCase: ResetUsedQuestionsUseCase,
    private val getThemeModeUseCase: GetThemeModeUseCase,
    private val setThemeModeUseCase: SetThemeModeUseCase,
    private val getQuestionsUseCase: GetQuestionsUseCase,
    private val getUsedQuestionIdsUseCase: GetUsedQuestionIdsUseCase,
    private val getContentLanguageUseCase: GetContentLanguageUseCase,
    private val getLanguageOverrideUseCase: GetLanguageOverrideUseCase,
    private val setLanguageOverrideUseCase: SetLanguageOverrideUseCase,
    private val getAnalyticsEnabledUseCase: GetAnalyticsEnabledUseCase,
    private val setAnalyticsEnabledUseCase: SetAnalyticsEnabledUseCase,
    private val analyticsTracker: AnalyticsTracker,
    private val updatesEnabled: Boolean = true,
) : ViewModel() {
    private val _uiState =
        MutableStateFlow(
            SettingsUiState(
                updatesEnabled = updatesEnabled,
                languageOverride = getLanguageOverrideUseCase(),
                analyticsEnabled = getAnalyticsEnabledUseCase(),
            ),
        )
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            getThemeModeUseCase().collect { themeMode ->
                _uiState.update { it.copy(themeMode = themeMode) }
            }
        }
    }

    fun onScreenEntered() {
        loadCardCounts()
        if (!updatesEnabled) return
        viewModelScope.launch {
            when (val result = checkForUpdateUseCase().getOrNull()) {
                is UpdateCheckResult.UpToDate ->
                    _uiState.update { it.copy(updateStatus = UpdateStatus.UpToDate(result.currentVersionName)) }
                is UpdateCheckResult.UpdateAvailable ->
                    _uiState.update { it.copy(updateStatus = UpdateStatus.UpdateAvailable(result.update)) }
                null -> Unit
            }
        }
    }

    /** Refreshes the "N of M hidden" count shown above the reset-cards button. */
    private fun loadCardCounts() {
        viewModelScope.launch {
            val languageCode = getContentLanguageUseCase()
            val totalCardsCount = getQuestionsUseCase(languageCode).size
            val hiddenCardsCount = getUsedQuestionIdsUseCase().size
            _uiState.update { it.copy(totalCardsCount = totalCardsCount, hiddenCardsCount = hiddenCardsCount) }
        }
    }

    fun onThemeModeSelected(themeMode: ThemeMode) {
        analyticsTracker.logEvent(
            AnalyticsEvents.THEME_CHANGED,
            mapOf(AnalyticsParams.THEME_MODE to themeMode.name.lowercase()),
        )
        viewModelScope.launch {
            setThemeModeUseCase(themeMode)
        }
    }

    /**
     * The "Share usage data" switch. Analytics is opt-out, so this starts on for a device that has
     * never touched it. Persisting the choice and acting on it are two separate steps on purpose:
     * [SetAnalyticsEnabledUseCase] only records it, while [AnalyticsTracker.setCollectionEnabled]
     * is what actually stops or restarts the SDK - and it also emits the opt-in/opt-out event
     * itself, because that event has to straddle the switch in the right order to be recorded at
     * all (see `ConsentAwareAnalyticsTracker`).
     *
     * The tracker is driven before the suspending write so an opt-out takes effect immediately
     * rather than after a round trip to storage.
     */
    fun onAnalyticsEnabledChanged(enabled: Boolean) {
        analyticsTracker.setCollectionEnabled(enabled)
        _uiState.update { it.copy(analyticsEnabled = enabled) }
        viewModelScope.launch {
            setAnalyticsEnabledUseCase(enabled)
        }
    }

    /** Reports a tap on one of the Settings screen's external links - see [AnalyticsParams.LINK]. */
    fun onOutboundLinkClicked(link: String) {
        analyticsTracker.logEvent(AnalyticsEvents.OUTBOUND_LINK_CLICKED, mapOf(AnalyticsParams.LINK to link))
    }

    /**
     * Sets (or, with `null`, clears) the in-app language override - see
     * [GetContentLanguageUseCase]. Only reachable from the language picker shown on iOS/Web (see
     * `SettingsScreen`'s use of `rememberOpenLanguageSettingsAction`); Android changes its language
     * through the OS's own per-app language settings instead. Card counts are reloaded immediately
     * since they're language-dependent; `HomeViewModel` picks the new language up on its own the
     * next time Home is entered (see its `onScreenEntered`).
     */
    fun onLanguageSelected(language: AppLanguage?) {
        analyticsTracker.logEvent(
            AnalyticsEvents.LANGUAGE_CHANGED,
            mapOf(AnalyticsParams.LANGUAGE to (language?.code ?: ANALYTICS_VALUE_AUTOMATIC)),
        )
        viewModelScope.launch {
            setLanguageOverrideUseCase(language)
            _uiState.update { it.copy(languageOverride = language) }
            loadCardCounts()
        }
    }

    /**
     * Checks GitHub Releases and, if a newer build exists, downloads it and launches the system
     * installer - unless [AppUpdateInstaller.canInstallPackages] says the OS will block that
     * install outright, in which case this stops at [UpdateStatus.SideloadingBlocked] without
     * downloading anything.
     */
    fun onUpdateClicked() {
        val availableUpdate = (_uiState.value.updateStatus as? UpdateStatus.UpdateAvailable)?.update
        if (availableUpdate != null) {
            viewModelScope.launch { downloadAndInstall(availableUpdate) }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(updateStatus = UpdateStatus.Checking) }
            checkForUpdateUseCase()
                .onSuccess { result -> handleCheckResultForUpdateClick(result) }
                .onFailure { error ->
                    logUpdateCheck(RESULT_FAILED)
                    _uiState.update { it.copy(updateStatus = UpdateStatus.Failed(error.toUserMessage())) }
                }
        }
    }

    /** Deep-links to the system "install unknown apps" settings page for this app. */
    fun onEnableSideloadingClicked() {
        appUpdateInstaller.openInstallPermissionSettings()
    }

    /** Makes every card hidden via swipe-down on Home visible again. */
    fun onResetCardsClicked() {
        analyticsTracker.logEvent(
            AnalyticsEvents.CARDS_RESET,
            mapOf(AnalyticsParams.HIDDEN_COUNT_BEFORE to _uiState.value.hiddenCardsCount),
        )
        viewModelScope.launch {
            _uiState.update { it.copy(resetCardsStatus = ResetCardsStatus.Resetting) }
            resetUsedQuestionsUseCase()
            _uiState.update { it.copy(resetCardsStatus = ResetCardsStatus.Done, hiddenCardsCount = 0) }
        }
    }

    private suspend fun handleCheckResultForUpdateClick(result: UpdateCheckResult) {
        when (result) {
            is UpdateCheckResult.UpToDate -> {
                logUpdateCheck(RESULT_UP_TO_DATE)
                _uiState.update { it.copy(updateStatus = UpdateStatus.UpToDate(result.currentVersionName)) }
            }
            is UpdateCheckResult.UpdateAvailable -> {
                logUpdateCheck(RESULT_AVAILABLE)
                downloadAndInstall(result.update)
            }
        }
    }

    private fun logUpdateCheck(result: String) {
        analyticsTracker.logEvent(AnalyticsEvents.UPDATE_CHECK, mapOf(AnalyticsParams.RESULT to result))
    }

    private suspend fun downloadAndInstall(update: AppUpdate) {
        if (!appUpdateInstaller.canInstallPackages()) {
            logUpdateCheck(RESULT_BLOCKED)
            _uiState.update { it.copy(updateStatus = UpdateStatus.SideloadingBlocked) }
            return
        }

        // Only the target version: AppUpdate doesn't carry the installed one, and Firebase
        // already segments every event by app version automatically, so recording it here would
        // duplicate a dimension the console provides for free.
        analyticsTracker.logEvent(
            AnalyticsEvents.UPDATE_DOWNLOAD_STARTED,
            mapOf(AnalyticsParams.TO_VERSION to update.versionName),
        )
        _uiState.update { it.copy(updateStatus = UpdateStatus.Downloading) }
        downloadAppUpdateUseCase(update)
            .onSuccess { apkFile ->
                appUpdateInstaller.installPackage(apkFile)
                _uiState.update { it.copy(updateStatus = UpdateStatus.Idle) }
            }.onFailure { error ->
                _uiState.update { it.copy(updateStatus = UpdateStatus.Failed(error.toUserMessage())) }
            }
    }

    private fun Throwable.toUserMessage(): String = message ?: "Something went wrong"

    companion object {
        const val RESULT_UP_TO_DATE = "up_to_date"
        const val RESULT_AVAILABLE = "available"
        const val RESULT_FAILED = "failed"
        const val RESULT_BLOCKED = "blocked"

        /** Values for [AnalyticsParams.LINK], one per external link on the Settings screen. */
        const val LINK_LOOPGAIN = "loopgain"
        const val LINK_PEDROVICENTE = "pedrovicente"
        const val LINK_GITHUB_RELEASES = "github_releases"
    }
}
