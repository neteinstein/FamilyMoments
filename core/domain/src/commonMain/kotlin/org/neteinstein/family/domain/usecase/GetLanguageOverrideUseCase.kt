package org.neteinstein.family.domain.usecase

import org.neteinstein.family.domain.model.AppLanguage
import org.neteinstein.family.domain.repository.LanguagePreferenceRepository

/** The user's current in-app language override, or `null` if none is set - see [GetContentLanguageUseCase]. */
class GetLanguageOverrideUseCase(
    private val languagePreferenceRepository: LanguagePreferenceRepository,
) {
    operator fun invoke(): AppLanguage? = languagePreferenceRepository.getLanguageOverride()
}
