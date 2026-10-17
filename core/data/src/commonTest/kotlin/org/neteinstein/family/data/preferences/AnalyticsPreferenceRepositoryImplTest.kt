package org.neteinstein.family.data.preferences

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.neteinstein.family.data.local.KeyValueStore
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

private class FakeStore : KeyValueStore {
    val values = mutableMapOf<String, String>()

    override fun getString(key: String): String? = values[key]

    override fun putString(
        key: String,
        value: String,
    ) {
        values[key] = value
    }
}

class AnalyticsPreferenceRepositoryImplTest {
    private val keyValueStore = FakeStore()
    private val repository = AnalyticsPreferenceRepositoryImpl(keyValueStore)

    @Test
    fun `defaults to enabled when nothing was ever set`() {
        assertTrue(repository.isAnalyticsEnabled())
    }

    @Test
    fun `setAnalyticsEnabled persists the choice`() =
        runTest {
            repository.setAnalyticsEnabled(false)

            assertFalse(repository.isAnalyticsEnabled())
            assertEquals("false", keyValueStore.values["analytics_enabled"])
        }

    @Test
    fun `a new instance reads the persisted choice back`() =
        runTest {
            repository.setAnalyticsEnabled(false)

            assertFalse(AnalyticsPreferenceRepositoryImpl(keyValueStore).isAnalyticsEnabled())
        }

    /**
     * The failure mode this guards against is a corrupted preference silently disabling collection
     * forever - anything unparseable must fall back to the opt-out default, not to false.
     */
    @Test
    fun `an unparseable stored value falls back to enabled`() {
        keyValueStore.putString("analytics_enabled", "definitely-not-a-boolean")

        assertTrue(AnalyticsPreferenceRepositoryImpl(keyValueStore).isAnalyticsEnabled())
    }

    @Test
    fun `analyticsEnabled starts at the persisted choice`() =
        runTest {
            keyValueStore.putString("analytics_enabled", "false")

            assertFalse(AnalyticsPreferenceRepositoryImpl(keyValueStore).analyticsEnabled.first())
        }

    @Test
    fun `analyticsEnabled emits every later change`() =
        runTest {
            val collected = mutableListOf<Boolean>()
            val job = launch { repository.analyticsEnabled.toList(collected) }
            runCurrent()

            repository.setAnalyticsEnabled(false)
            runCurrent()
            repository.setAnalyticsEnabled(true)
            runCurrent()
            job.cancel()

            assertEquals(listOf(true, false, true), collected)
        }
}
