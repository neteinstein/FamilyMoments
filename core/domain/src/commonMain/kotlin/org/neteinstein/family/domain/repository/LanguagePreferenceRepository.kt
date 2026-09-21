package org.neteinstein.family.domain.repository

import kotlinx.coroutines.flow.Flow
import org.neteinstein.family.domain.model.AppLanguage

/**
 * The user's explicit in-app language choice, made from the Settings screen's language picker -
 * distinct from [LocaleProvider], which only ever reflects the OS/browser's own locale. `null`
 * means "no override" - the app should fall back to [LocaleProvider] (see
 * [org.neteinstein.family.domain.usecase.GetContentLanguageUseCase]).
 *
 * The override drives both the question content's language and the app's own UI strings (see
 * `core:ui`'s `ProvideAppLanguage`, wired up in `app`'s `App()` off [languageOverride]).
 *
 * On Android, changing the language is done by deep-linking into the system's per-app language
 * settings (see `feature:settings`'s `rememberOpenLanguageSettingsAction`), which changes what
 * [LocaleProvider] itself reports - this repository's override is only ever set from iOS/Web,
 * which have no such OS-level settings page to deep-link into.
 */
interface LanguagePreferenceRepository {
    /**
     * Emits the current override (and every later change to it), so UI that has to re-render on a
     * language change - the whole app, for [languageOverride]'s effect on resource lookups - can
     * observe it rather than re-reading [getLanguageOverride] at arbitrary moments.
     */
    val languageOverride: Flow<AppLanguage?>

    fun getLanguageOverride(): AppLanguage?

    suspend fun setLanguageOverride(language: AppLanguage?)
}
