package org.neteinstein.family.data.analytics

import android.os.Bundle
import com.google.firebase.FirebaseApp
import com.google.firebase.analytics.FirebaseAnalytics
import org.neteinstein.family.data.local.AndroidAppContext
import org.neteinstein.family.domain.analytics.AnalyticsParams
import org.neteinstein.family.domain.analytics.AnalyticsTracker

/**
 * Firebase Analytics on Android.
 *
 * **Everything here is conditional on Firebase actually having initialized.** `google-services.json`
 * is git-ignored, so a clean checkout, a fork, or a fork-originated PR builds an app in which the
 * google-services plugin never ran, no `google_app_id` string resource exists, and
 * `FirebaseInitProvider` therefore left no default `FirebaseApp` behind. Touching
 * `FirebaseAnalytics.getInstance` in that state throws at startup, so [analytics] is resolved
 * lazily and defensively and every method degrades to a no-op - see [AnalyticsTracker]'s kdoc on
 * why analytics must never take a screen down with it.
 *
 * The context comes from [AndroidAppContext] rather than Koin's `androidContext()`, matching how
 * this module's Room and SharedPreferences actuals already reach it: `commonMain` code can never
 * call an Android-only Koin API, so `app`'s `setAndroidAppContext` sets it before Koin resolves
 * anything (see `FamilyMomentsApp.onCreate`).
 */
class FirebaseAnalyticsTracker : AnalyticsTracker {
    private val analytics: FirebaseAnalytics? by lazy {
        runCatching {
            // AndroidAppContext.instance is a lateinit var: reading it before
            // FamilyMomentsApp.onCreate has run throws UninitializedPropertyAccessException, which
            // this runCatching swallows along with everything else below.
            val context = AndroidAppContext.instance
            if (FirebaseApp.getApps(context).isEmpty()) {
                null
            } else {
                FirebaseAnalytics.getInstance(context)
            }
        }.getOrNull()
    }

    override fun logScreenView(screenName: String) {
        logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, mapOf(AnalyticsParams.SCREEN_NAME to screenName))
    }

    override fun logEvent(
        name: String,
        params: Map<String, Any?>,
    ) {
        analytics?.logEvent(name, params.toBundle())
    }

    override fun setUserProperty(
        name: String,
        value: String?,
    ) {
        analytics?.setUserProperty(name, value)
    }

    override fun setCollectionEnabled(enabled: Boolean) {
        analytics?.setAnalyticsCollectionEnabled(enabled)
    }
}

/**
 * Only the types Firebase itself accepts survive: numbers stay numbers so they remain summable
 * *metrics* in the console rather than string dimensions. Anything else - `null` included - is
 * dropped rather than stringified, so a mistake shows up as a missing parameter in DebugView
 * instead of a plausible-looking wrong value.
 */
private fun Map<String, Any?>.toBundle(): Bundle =
    Bundle().apply {
        forEach { (key, value) ->
            when (value) {
                is String -> putString(key, value)
                is Int -> putLong(key, value.toLong())
                is Long -> putLong(key, value)
                is Double -> putDouble(key, value)
                is Float -> putDouble(key, value.toDouble())
                is Boolean -> putString(key, value.toString())
                else -> Unit
            }
        }
    }
