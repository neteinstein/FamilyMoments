package org.neteinstein.family.domain.usecase

import kotlinx.coroutines.flow.Flow
import org.neteinstein.family.domain.model.ThemeMode
import org.neteinstein.family.domain.repository.ThemePreferenceRepository

class GetThemeModeUseCase(
    private val themePreferenceRepository: ThemePreferenceRepository,
) {
    operator fun invoke(): Flow<ThemeMode> = themePreferenceRepository.themeMode
}
