package org.neteinstein.family.data.preferences

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.neteinstein.family.data.local.KeyValueStore
import org.neteinstein.family.domain.repository.AnalyticsPreferenceRepository

private const val ANALYTICS_ENABLED_KEY = "analytics_enabled"

/**
 * Mirrors [ThemePreferenceRepositoryImpl] exactly, including its cached-StateFlow approach: a
 * single Koin `single` instance is the only writer this app has, so a locally cached StateFlow
 * updated on every write is equivalent to a reactive store without needing a reactive
 * [KeyValueStore].
 *
 * **Defaults to `true`** - analytics is opt-out, so an absent key means "never asked, collect".
 * Any value other than the two booleans' `toString()` forms is treated as absent rather than
 * false, so a corrupted preference can't silently turn collection off forever.
 */
class AnalyticsPreferenceRepositoryImpl(
    private val keyValueStore: KeyValueStore,
) : AnalyticsPreferenceRepository {
    private val analyticsEnabledState = MutableStateFlow(readAnalyticsEnabled())

    override val analyticsEnabled: Flow<Boolean> = analyticsEnabledState.asStateFlow()

    override fun isAnalyticsEnabled(): Boolean = analyticsEnabledState.value

    override suspend fun setAnalyticsEnabled(enabled: Boolean) {
        keyValueStore.putString(ANALYTICS_ENABLED_KEY, enabled.toString())
        analyticsEnabledState.value = enabled
    }

    private fun readAnalyticsEnabled(): Boolean = keyValueStore.getString(ANALYTICS_ENABLED_KEY)?.toBooleanStrictOrNull() ?: true
}
