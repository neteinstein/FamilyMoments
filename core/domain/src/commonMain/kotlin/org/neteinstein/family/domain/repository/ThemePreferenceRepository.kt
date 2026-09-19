package org.neteinstein.family.domain.repository

import kotlinx.coroutines.flow.Flow
import org.neteinstein.family.domain.model.ThemeMode

/**
 * Persists the user's chosen app appearance ([ThemeMode.SYSTEM]/[ThemeMode.LIGHT]/[ThemeMode.DARK]),
 * set from the Settings screen. Declared here (rather than in `core:data` directly) so
 * `feature:settings` and the `app` module can depend on it without depending on `core:data`, per
 * this project's module boundary rules - the real implementation lives in `core:data`.
 */
interface ThemePreferenceRepository {
    val themeMode: Flow<ThemeMode>

    suspend fun setThemeMode(themeMode: ThemeMode)
}
