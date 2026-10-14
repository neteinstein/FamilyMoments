package org.neteinstein.family.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.neteinstein.family.domain.model.AppLanguage
import org.neteinstein.family.domain.model.AppUpdate
import org.neteinstein.family.domain.model.ThemeMode
import org.neteinstein.family.domain.model.UpdateCheckResult
import org.neteinstein.family.domain.repository.AppUpdateInstaller
import org.neteinstein.family.domain.usecase.CheckForUpdateUseCase
import org.neteinstein.family.domain.usecase.DownloadAppUpdateUseCase
import org.neteinstein.family.domain.usecase.GetContentLanguageUseCase
import org.neteinstein.family.domain.usecase.GetLanguageOverrideUseCase
import org.neteinstein.family.domain.usecase.GetQuestionsUseCase
import org.neteinstein.family.domain.usecase.GetThemeModeUseCase
import org.neteinstein.family.domain.usecase.GetUsedQuestionIdsUseCase
import org.neteinstein.family.domain.usecase.ResetUsedQuestionsUseCase
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
    private val updatesEnabled: Boolean = true,
) : ViewModel() {
    private val _uiState =
        MutableStateFlow(
            SettingsUiState(updatesEnabled = updatesEnabled, languageOverride = getLanguageOverrideUseCase()),
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
        viewModelScope.launch {
            setThemeModeUseCase(themeMode)
        }
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
        viewModelScope.launch {
            _uiState.update { it.copy(resetCardsStatus = ResetCardsStatus.Resetting) }
            resetUsedQuestionsUseCase()
            _uiState.update { it.copy(resetCardsStatus = ResetCardsStatus.Done, hiddenCardsCount = 0) }
        }
    }

    private suspend fun handleCheckResultForUpdateClick(result: UpdateCheckResult) {
        when (result) {
            is UpdateCheckResult.UpToDate ->
                _uiState.update { it.copy(updateStatus = UpdateStatus.UpToDate(result.currentVersionName)) }
            is UpdateCheckResult.UpdateAvailable -> downloadAndInstall(result.update)
        }
    }

    private suspend fun downloadAndInstall(update: AppUpdate) {
        if (!appUpdateInstaller.canInstallPackages()) {
            _uiState.update { it.copy(updateStatus = UpdateStatus.SideloadingBlocked) }
            return
        }

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
}
