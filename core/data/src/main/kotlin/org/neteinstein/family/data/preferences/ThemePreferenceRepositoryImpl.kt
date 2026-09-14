package org.neteinstein.family.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.neteinstein.family.domain.model.ThemeMode
import org.neteinstein.family.domain.repository.ThemePreferenceRepository

private val Context.themeDataStore: DataStore<Preferences> by preferencesDataStore(name = "theme_preferences")
private val THEME_MODE_KEY = stringPreferencesKey("theme_mode")

class ThemePreferenceRepositoryImpl(
    private val context: Context,
) : ThemePreferenceRepository {
    override val themeMode: Flow<ThemeMode> =
        context.themeDataStore.data.map { preferences ->
            preferences[THEME_MODE_KEY]?.let { name ->
                runCatching { ThemeMode.valueOf(name) }.getOrNull()
            } ?: ThemeMode.SYSTEM
        }

    override suspend fun setThemeMode(themeMode: ThemeMode) {
        context.themeDataStore.edit { preferences ->
            preferences[THEME_MODE_KEY] = themeMode.name
        }
    }
}
