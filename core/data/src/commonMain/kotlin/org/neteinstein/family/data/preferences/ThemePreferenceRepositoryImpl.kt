package org.neteinstein.family.data.preferences

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.neteinstein.family.data.local.KeyValueStore
import org.neteinstein.family.domain.model.ThemeMode
import org.neteinstein.family.domain.repository.ThemePreferenceRepository

private const val THEME_MODE_KEY = "theme_mode"

class ThemePreferenceRepositoryImpl(
    private val keyValueStore: KeyValueStore,
) : ThemePreferenceRepository {
    // A single Koin `single` instance is the only writer this app has, so a locally cached
    // StateFlow (updated on every setThemeMode) is equivalent to DataStore's reactive Flow here
    // without needing a reactive KeyValueStore.
    private val themeModeState = MutableStateFlow(readThemeMode())

    override val themeMode: Flow<ThemeMode> = themeModeState.asStateFlow()

    override suspend fun setThemeMode(themeMode: ThemeMode) {
        keyValueStore.putString(THEME_MODE_KEY, themeMode.name)
        themeModeState.value = themeMode
    }

    private fun readThemeMode(): ThemeMode =
        keyValueStore.getString(THEME_MODE_KEY)?.let { name ->
            runCatching { ThemeMode.valueOf(name) }.getOrNull()
        } ?: ThemeMode.SYSTEM
}
