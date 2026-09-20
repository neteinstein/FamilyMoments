package org.neteinstein.family.data.local

/**
 * Tiny cross-platform key-value abstraction backing [org.neteinstein.family.data.preferences.
 * ThemePreferenceRepositoryImpl] - replaces AndroidX DataStore (Android-only in this app until
 * now), which doesn't support wasmJs. Actuals: Android `SharedPreferences`, iOS `NSUserDefaults`,
 * wasmJs an in-memory map for now (not yet persisted across page reloads - see
 * `QuestionLocalDataSource`'s wasmJs actual for the same interim choice, on the same schedule).
 */
interface KeyValueStore {
    fun getString(key: String): String?

    fun putString(
        key: String,
        value: String,
    )
}

expect fun platformKeyValueStore(): KeyValueStore
