package org.neteinstein.family.data.analytics

import org.neteinstein.family.domain.analytics.AnalyticsEvents
import org.neteinstein.family.domain.analytics.AnalyticsTracker
import org.neteinstein.family.domain.repository.AnalyticsPreferenceRepository

/**
 * The [AnalyticsTracker] the rest of the app actually gets from Koin: it wraps the platform
 * tracker and honours the Settings screen's "Share usage data" switch.
 *
 * Two independent mechanisms, on purpose:
 *
 * 1. Every call is dropped here while the user is opted out, so nothing reaches the SDK at all.
 * 2. [setCollectionEnabled] is pushed down to the delegate, so the SDK itself stops buffering and
 *    uploading. This is the one that actually stops network traffic - dropping calls upstream
 *    would not prevent Firebase from sending the automatic events it collects on its own
 *    (`session_start`, `user_engagement`, `first_open`).
 *
 * The delegate is synced on construction rather than lazily, so a device that opted out in a
 * previous run never uploads an automatic event in the window before the first Settings visit.
 *
 * [setCollectionEnabled] also emits the opt-in/opt-out events itself rather than leaving that to
 * its caller, because their ordering around the switch is the whole subtlety: an opt-*out* event
 * has to be logged before collection stops or it would never leave the device, and an opt-*in*
 * event after collection restarts, for the same reason in reverse. Both go straight to the
 * delegate, bypassing this class's own consent check - at the instant of the transition the check
 * would reject exactly the event being recorded. Keeping this here also means `feature:settings`
 * only ever touches the `AnalyticsTracker` interface in `core:domain`, never this class.
 */
class ConsentAwareAnalyticsTracker(
    private val delegate: AnalyticsTracker,
    private val preferences: AnalyticsPreferenceRepository,
) : AnalyticsTracker {
    init {
        delegate.setCollectionEnabled(preferences.isAnalyticsEnabled())
    }

    override fun logScreenView(screenName: String) {
        if (preferences.isAnalyticsEnabled()) delegate.logScreenView(screenName)
    }

    override fun logEvent(
        name: String,
        params: Map<String, Any?>,
    ) {
        if (preferences.isAnalyticsEnabled()) delegate.logEvent(name, params)
    }

    override fun setUserProperty(
        name: String,
        value: String?,
    ) {
        if (preferences.isAnalyticsEnabled()) delegate.setUserProperty(name, value)
    }

    override fun setCollectionEnabled(enabled: Boolean) {
        if (enabled) {
            delegate.setCollectionEnabled(true)
            delegate.logEvent(AnalyticsEvents.ANALYTICS_OPT_IN)
        } else {
            delegate.logEvent(AnalyticsEvents.ANALYTICS_OPT_OUT)
            delegate.setCollectionEnabled(false)
        }
    }
}
