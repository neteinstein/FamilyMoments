package org.neteinstein.family.data.local

/**
 * Backed by the browser's `localStorage` so preferences (theme, language override) survive a page
 * reload - a single string key per preference is a small enough surface to wire up directly, unlike
 * [org.neteinstein.family.data.local.QuestionLocalDataSource]'s wasmJs actual, which needed a real
 * IndexedDB-backed implementation (see that actual) for its larger, queryable row shape.
 */
private class LocalStorageKeyValueStore : KeyValueStore {
    override fun getString(key: String): String? = readLocalStorageItem(key)

    override fun putString(
        key: String,
        value: String,
    ) {
        writeLocalStorageItem(key, value)
    }
}

actual fun platformKeyValueStore(): KeyValueStore = LocalStorageKeyValueStore()

private fun readLocalStorageItem(key: String): String? = js("localStorage.getItem(key)")

private fun writeLocalStorageItem(
    key: String,
    value: String,
): Unit = js("localStorage.setItem(key, value)")
