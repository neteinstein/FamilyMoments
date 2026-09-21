package org.neteinstein.family.feature.settings

import org.neteinstein.family.domain.model.AppLanguage
import org.neteinstein.family.domain.model.AppUpdate
import org.neteinstein.family.domain.model.ThemeMode

data class SettingsUiState(
    val updateStatus: UpdateStatus = UpdateStatus.Idle,
    val resetCardsStatus: ResetCardsStatus = ResetCardsStatus.Idle,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    /** False on the Play Store flavor, which hides the "Updates" section entirely - see SettingsViewModel. */
    val updatesEnabled: Boolean = true,
    /** Number of cards hidden via swipe-down on Home, out of [totalCardsCount]. */
    val hiddenCardsCount: Int = 0,
    val totalCardsCount: Int = 0,
    /** The user's in-app language override, or `null` to follow the OS/browser language - see SettingsViewModel. */
    val languageOverride: AppLanguage? = null,
    /**
     * The "Share usage data" switch. Analytics is opt-out, so this is true until the user says
     * otherwise - see SettingsViewModel.onAnalyticsEnabledChanged and PRIVACY_POLICY.md.
     */
    val analyticsEnabled: Boolean = true,
)

/** Drives the "Reset Cards" button and its status text on the Settings screen. */
sealed class ResetCardsStatus {
    data object Idle : ResetCardsStatus()

    data object Resetting : ResetCardsStatus()

    data object Done : ResetCardsStatus()
}

/** Drives the "Update to latest" button and its status text on the Settings screen. */
sealed class UpdateStatus {
    /** Nothing in flight - the button's normal resting state. */
    data object Idle : UpdateStatus()

    data object Checking : UpdateStatus()

    /** The installed build is already the latest one published on GitHub Releases. */
    data class UpToDate(
        val currentVersionName: String,
    ) : UpdateStatus()

    /** A newer build exists and is ready to be downloaded/installed on button tap. */
    data class UpdateAvailable(
        val update: AppUpdate,
    ) : UpdateStatus()

    data object Downloading : UpdateStatus()

    /**
     * A newer release exists, but the OS won't let this app install it yet - see
     * `AppUpdateInstaller.canInstallPackages`. The Settings screen shows a warning whose action
     * opens the system "install unknown apps" page for this app
     * ([SettingsViewModel.onEnableSideloadingClicked]); the user is expected to tap "Update to
     * latest" again afterwards, which re-checks and proceeds automatically now that the OS allows
     * it - there's no lifecycle-based auto-recheck of this state.
     */
    data object SideloadingBlocked : UpdateStatus()

    data class Failed(
        val message: String,
    ) : UpdateStatus()
}
