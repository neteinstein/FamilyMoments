package org.neteinstein.family.data.preferences

import org.neteinstein.family.data.local.KeyValueStore
import org.neteinstein.family.domain.model.AppLanguage
import org.neteinstein.family.domain.repository.LanguagePreferenceRepository

private const val LANGUAGE_OVERRIDE_KEY = "language_override"

class LanguagePreferenceRepositoryImpl(
    private val keyValueStore: KeyValueStore,
) : LanguagePreferenceRepository {
    override fun getLanguageOverride(): AppLanguage? = keyValueStore.getString(LANGUAGE_OVERRIDE_KEY)?.let { AppLanguage.fromCode(it) }

    override suspend fun setLanguageOverride(language: AppLanguage?) {
        keyValueStore.putString(LANGUAGE_OVERRIDE_KEY, language?.code ?: "")
    }
}
