package org.neteinstein.family

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import org.neteinstein.family.domain.model.ThemeMode
import org.neteinstein.family.domain.usecase.GetThemeModeUseCase

/**
 * Exposes the persisted theme-mode preference so [MainActivity] can resolve it into the boolean
 * `FamilyMomentsTheme`'s `darkTheme` parameter expects.
 */
class MainActivityViewModel(
    getThemeModeUseCase: GetThemeModeUseCase,
) : ViewModel() {
    val themeMode: StateFlow<ThemeMode> =
        getThemeModeUseCase()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ThemeMode.SYSTEM)
}
