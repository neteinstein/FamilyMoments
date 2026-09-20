package org.neteinstein.family.data.preferences

import kotlinx.coroutines.test.runTest
import org.neteinstein.family.data.local.KeyValueStore
import org.neteinstein.family.domain.model.AppLanguage
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

private class FakeKeyValueStore : KeyValueStore {
    private val values = mutableMapOf<String, String>()

    override fun getString(key: String): String? = values[key]

    override fun putString(
        key: String,
        value: String,
    ) {
        values[key] = value
    }
}

class LanguagePreferenceRepositoryImplTest {
    private val keyValueStore = FakeKeyValueStore()
    private val repository = LanguagePreferenceRepositoryImpl(keyValueStore)

    @Test
    fun `getLanguageOverride returns null when nothing was ever set`() {
        assertNull(repository.getLanguageOverride())
    }

    @Test
    fun `setLanguageOverride persists the chosen language`() =
        runTest {
            repository.setLanguageOverride(AppLanguage.FRENCH)

            assertEquals(AppLanguage.FRENCH, repository.getLanguageOverride())
        }

    @Test
    fun `setLanguageOverride with null clears a previously persisted override`() =
        runTest {
            repository.setLanguageOverride(AppLanguage.GERMAN)
            repository.setLanguageOverride(null)

            assertNull(repository.getLanguageOverride())
        }
}
