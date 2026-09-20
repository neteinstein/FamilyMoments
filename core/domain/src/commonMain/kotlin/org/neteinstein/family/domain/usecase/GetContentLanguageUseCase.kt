package org.neteinstein.family.domain.usecase

import org.neteinstein.family.domain.model.AppLanguage
import org.neteinstein.family.domain.repository.LanguagePreferenceRepository
import org.neteinstein.family.domain.repository.LocaleProvider

/**
 * Resolves the language question content should load in: the user's explicit in-app override if
 * one is set, otherwise the OS/browser locale ([LocaleProvider]) if it's one of [AppLanguage]'s
 * supported codes, otherwise [AppLanguage.Default]. This is the single source of truth
 * `HomeViewModel`/`SettingsViewModel` call instead of reading [LocaleProvider] directly, so both
 * screens agree on which language is active.
 */
class GetContentLanguageUseCase(
    private val localeProvider: LocaleProvider,
    private val languagePreferenceRepository: LanguagePreferenceRepository,
) {
    operator fun invoke(): String =
        languagePreferenceRepository.getLanguageOverride()?.code
            ?: AppLanguage.fromCode(localeProvider.currentLanguageCode())?.code
            ?: AppLanguage.Default.code
}
