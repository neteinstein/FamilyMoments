package org.neteinstein.family.domain.usecase

import kotlinx.coroutines.flow.Flow
import org.neteinstein.family.domain.model.AppLanguage
import org.neteinstein.family.domain.repository.LanguagePreferenceRepository

/**
 * The user's in-app language override as a stream - `null` while no override is set. `app`'s
 * `MainActivityViewModel` collects this so `App()` can re-resolve the whole UI's string resources
 * as soon as the Settings picker changes the language, without waiting for a restart.
 */
class ObserveLanguageOverrideUseCase(
    private val languagePreferenceRepository: LanguagePreferenceRepository,
) {
    operator fun invoke(): Flow<AppLanguage?> = languagePreferenceRepository.languageOverride
}
