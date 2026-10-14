package org.neteinstein.family.data.preferences

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.neteinstein.family.data.local.KeyValueStore
import org.neteinstein.family.domain.model.AppLanguage
import org.neteinstein.family.domain.repository.LanguagePreferenceRepository

private const val LANGUAGE_OVERRIDE_KEY = "language_override"

class LanguagePreferenceRepositoryImpl(
    private val keyValueStore: KeyValueStore,
) : LanguagePreferenceRepository {
    // Same approach as ThemePreferenceRepositoryImpl: a single Koin `single` instance is this
    // app's only writer, so a locally cached StateFlow updated on every write is equivalent to a
    // reactive store without needing a reactive KeyValueStore.
    private val languageOverrideState = MutableStateFlow(readLanguageOverride())

    override val languageOverride: Flow<AppLanguage?> = languageOverrideState.asStateFlow()

    override fun getLanguageOverride(): AppLanguage? = languageOverrideState.value

    override suspend fun setLanguageOverride(language: AppLanguage?) {
        keyValueStore.putString(LANGUAGE_OVERRIDE_KEY, language?.code ?: "")
        languageOverrideState.value = language
    }

    private fun readLanguageOverride(): AppLanguage? = keyValueStore.getString(LANGUAGE_OVERRIDE_KEY)?.let { AppLanguage.fromCode(it) }
}
