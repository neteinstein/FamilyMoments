package org.neteinstein.family.domain.usecase

import org.neteinstein.family.domain.model.ThemeMode
import org.neteinstein.family.domain.repository.ThemePreferenceRepository

class SetThemeModeUseCase(
    private val themePreferenceRepository: ThemePreferenceRepository,
) {
    suspend operator fun invoke(themeMode: ThemeMode) = themePreferenceRepository.setThemeMode(themeMode)
}
