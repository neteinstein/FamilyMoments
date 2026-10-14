package org.neteinstein.family.data.local

/**
 * Tiny cross-platform key-value abstraction backing [org.neteinstein.family.data.preferences.
 * ThemePreferenceRepositoryImpl] - replaces AndroidX DataStore (Android-only in this app until
 * now), which doesn't support wasmJs. Actuals: Android `SharedPreferences`, iOS `NSUserDefaults`,
 * wasmJs `window.localStorage` (persisted across page reloads).
 */
interface KeyValueStore {
    fun getString(key: String): String?

    fun putString(
        key: String,
        value: String,
    )
}

expect fun platformKeyValueStore(): KeyValueStore
