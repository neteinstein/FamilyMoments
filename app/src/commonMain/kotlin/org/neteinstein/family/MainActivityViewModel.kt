package org.neteinstein.family

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import org.neteinstein.family.domain.model.AppLanguage
import org.neteinstein.family.domain.model.ThemeMode
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
 */
class MainActivityViewModel(
    getThemeModeUseCase: GetThemeModeUseCase,
    getLanguageOverrideUseCase: GetLanguageOverrideUseCase,
    observeLanguageOverrideUseCase: ObserveLanguageOverrideUseCase,
) : ViewModel() {
    val themeMode: StateFlow<ThemeMode> =
        getThemeModeUseCase()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ThemeMode.SYSTEM)

    val languageOverride: StateFlow<AppLanguage?> =
        observeLanguageOverrideUseCase()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), getLanguageOverrideUseCase())
}
