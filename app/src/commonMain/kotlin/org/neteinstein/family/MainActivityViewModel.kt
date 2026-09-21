package org.neteinstein.family

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.neteinstein.family.domain.analytics.ANALYTICS_VALUE_AUTOMATIC
import org.neteinstein.family.domain.analytics.AnalyticsEvents
import org.neteinstein.family.domain.analytics.AnalyticsParams
import org.neteinstein.family.domain.analytics.AnalyticsTracker
import org.neteinstein.family.domain.analytics.AnalyticsUserProperties
import org.neteinstein.family.domain.model.AppLanguage
import org.neteinstein.family.domain.model.ThemeMode
import org.neteinstein.family.domain.usecase.GetContentLanguageUseCase
import org.neteinstein.family.domain.usecase.GetLanguageOverrideUseCase
import org.neteinstein.family.domain.usecase.GetThemeModeUseCase
import org.neteinstein.family.domain.usecase.ObserveLanguageOverrideUseCase

/**
 * Exposes the two app-wide preferences [App] applies around the whole UI: the theme mode (resolved
 * there into the boolean `FamilyMomentsTheme`'s `darkTheme` parameter expects) and the in-app
 * language override (fed to `ProvideAppLanguage`, so switching language in Settings re-resolves
 * every string resource immediately). The language override's initial value is read synchronously
 * so the first frame is already in the right language rather than flashing the OS locale's
 * strings.
 *
 * It also owns the app-wide analytics user properties, because it already holds exactly the state
 * they describe. The two static ones are set once; the two preference-backed ones re-collect the
 * same flows the UI does, so a change in Settings updates the segmentation immediately rather than
 * at next launch. Per-screen state (the hidden-card bucket) is set by the screen that knows it -
 * see feature:home's HomeViewModel.
 *
 * Nothing here identifies a person: there is no user ID, and [AnalyticsUserProperties] carries
 * coarse dimensions only.
 */
class MainActivityViewModel(
    getThemeModeUseCase: GetThemeModeUseCase,
    getLanguageOverrideUseCase: GetLanguageOverrideUseCase,
    observeLanguageOverrideUseCase: ObserveLanguageOverrideUseCase,
    getContentLanguageUseCase: GetContentLanguageUseCase,
    private val analyticsTracker: AnalyticsTracker,
    platform: String,
    distribution: String?,
) : ViewModel() {
    val themeMode: StateFlow<ThemeMode> =
        getThemeModeUseCase()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ThemeMode.SYSTEM)

    val languageOverride: StateFlow<AppLanguage?> =
        observeLanguageOverrideUseCase()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), getLanguageOverrideUseCase())

    init {
        analyticsTracker.setUserProperty(AnalyticsUserProperties.PLATFORM, platform)
        // Left unset rather than defaulted on iOS/Web, where there are no distribution channels.
        distribution?.let { analyticsTracker.setUserProperty(AnalyticsUserProperties.DISTRIBUTION, it) }
        analyticsTracker.setUserProperty(AnalyticsUserProperties.CONTENT_LANGUAGE, getContentLanguageUseCase())

        // Collected rather than read once: a change in Settings must re-segment this session, not
        // the next one. Both flows are the same ones the UI collects.
        viewModelScope.launch {
            themeMode.collect { mode ->
                analyticsTracker.setUserProperty(AnalyticsUserProperties.THEME_MODE, mode.name.lowercase())
            }
        }
        viewModelScope.launch {
            languageOverride.collect { language ->
                analyticsTracker.setUserProperty(
                    AnalyticsUserProperties.APP_LANGUAGE_OVERRIDE,
                    language?.code ?: ANALYTICS_VALUE_AUTOMATIC,
                )
            }
        }
    }

    /** Reports a screen view. Called from [App]'s navigation observer - see its comment there. */
    fun onScreenViewed(screenName: String) {
        analyticsTracker.logScreenView(screenName)
    }

    /**
     * Reports what the user did with the Web-only "install the Android app" banner - `shown`,
     * `clicked` or `dismissed`. `core:ui` can't inject the tracker itself (it depends on nothing
     * but Compose), so the banner hands the action up here instead.
     */
    fun onInstallBannerAction(action: String) {
        analyticsTracker.logEvent(AnalyticsEvents.INSTALL_BANNER, mapOf(AnalyticsParams.ACTION to action))
    }
}
