package org.neteinstein.family.data.local

/**
 * In-memory only for now - not yet persisted across a page reload. See
 * [QuestionLocalDataSource]'s wasmJs actual for the same interim choice and its rationale.
 */
private class InMemoryKeyValueStore : KeyValueStore {
    private val values = mutableMapOf<String, String>()

    override fun getString(key: String): String? = values[key]

    override fun putString(
        key: String,
        value: String,
    ) {
        values[key] = value
    }
}

actual fun platformKeyValueStore(): KeyValueStore = InMemoryKeyValueStore()
