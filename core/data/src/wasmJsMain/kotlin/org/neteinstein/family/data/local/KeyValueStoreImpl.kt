package org.neteinstein.family.data.local

import kotlin.js.ExperimentalWasmJsInterop

@OptIn(ExperimentalWasmJsInterop::class)
@JsFun("(key) => window.localStorage.getItem(key)")
private external fun jsLocalStorageGetItem(key: String): String?

@OptIn(ExperimentalWasmJsInterop::class)
@JsFun("(key, value) => window.localStorage.setItem(key, value)")
private external fun jsLocalStorageSetItem(
    key: String,
    value: String,
)

/** `window.localStorage`-backed - persists across page reloads, scoped to the page's origin. */
private class LocalStorageKeyValueStore : KeyValueStore {
    @OptIn(ExperimentalWasmJsInterop::class)
    override fun getString(key: String): String? = jsLocalStorageGetItem(key)

    @OptIn(ExperimentalWasmJsInterop::class)
    override fun putString(
        key: String,
        value: String,
    ) {
        jsLocalStorageSetItem(key, value)
    }
}

actual fun platformKeyValueStore(): KeyValueStore = LocalStorageKeyValueStore()
