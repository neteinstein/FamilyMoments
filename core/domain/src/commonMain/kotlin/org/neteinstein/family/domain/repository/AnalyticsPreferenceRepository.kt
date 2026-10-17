package org.neteinstein.family.domain.repository

import kotlinx.coroutines.flow.Flow

/**
 * Persists the user's "Share usage data" choice from the Settings screen. Analytics is **opt-out**:
 * this defaults to `true` on a device that has never expressed a preference, and the Settings
 * switch is the only thing that ever writes it.
 *
 * Declared here (rather than in `core:data` directly) so `feature:settings` and the `app` module
 * can depend on it without depending on `core:data`, per this project's module boundary rules -
 * the real implementation lives in `core:data`, on top of the same `KeyValueStore` that already
 * backs the theme and language preferences.
 */
interface AnalyticsPreferenceRepository {
    val analyticsEnabled: Flow<Boolean>

    /** Reads the current value without collecting - used by `ConsentAwareAnalyticsTracker`'s hot path. */
    fun isAnalyticsEnabled(): Boolean

    suspend fun setAnalyticsEnabled(enabled: Boolean)
}
