package org.neteinstein.family.data.local

import platform.Foundation.NSUserDefaults

actual fun platformKeyValueStore(): KeyValueStore = IosKeyValueStore()

private class IosKeyValueStore : KeyValueStore {
    private val defaults = NSUserDefaults.standardUserDefaults

    override fun getString(key: String): String? = defaults.stringForKey(key)

    override fun putString(
        key: String,
        value: String,
    ) {
        defaults.setObject(value, forKey = key)
    }
}
