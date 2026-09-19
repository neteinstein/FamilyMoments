package org.neteinstein.family.data.local

import android.content.Context

private const val PREFS_NAME = "family_moments_prefs"

actual fun platformKeyValueStore(): KeyValueStore = AndroidKeyValueStore(AndroidAppContext.instance)

private class AndroidKeyValueStore(
    context: Context,
) : KeyValueStore {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    override fun getString(key: String): String? = prefs.getString(key, null)

    override fun putString(
        key: String,
        value: String,
    ) {
        prefs.edit().putString(key, value).apply()
    }
}
