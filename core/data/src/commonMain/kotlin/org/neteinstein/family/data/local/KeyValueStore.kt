package org.neteinstein.family.data.local

/**
 * Tiny cross-platform key-value abstraction backing [org.neteinstein.family.data.preferences.
 * ThemePreferenceRepositoryImpl] and [org.neteinstein.family.data.preferences.
 * LanguagePreferenceRepositoryImpl] - replaces AndroidX DataStore (Android-only in this app until
 * now), which doesn't support wasmJs. Actuals: Android `SharedPreferences`, iOS `NSUserDefaults`,
 * wasmJs the browser's `localStorage`.
 */
interface KeyValueStore {
    fun getString(key: String): String?

    fun putString(
        key: String,
        value: String,
    )
}

expect fun platformKeyValueStore(): KeyValueStore
