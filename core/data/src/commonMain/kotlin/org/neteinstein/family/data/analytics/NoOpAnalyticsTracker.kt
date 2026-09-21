package org.neteinstein.family.data.analytics

import org.neteinstein.family.domain.analytics.AnalyticsTracker

/**
 * Does nothing at all. Bound on iOS, where no Firebase app is registered for this project (the
 * provided `google-services.json` declares Android clients only, and no `GoogleService-Info.plist`
 * exists), and used as the delegate fallback wherever a platform SDK turns out to be unavailable
 * at runtime - a debug build with no `google-services.json`, or a web build served without a
 * generated `firebase-init.js`.
 *
 * Analytics is best-effort by design (see [AnalyticsTracker]'s kdoc), so "no backend configured"
 * must be indistinguishable from "backend configured" as far as the rest of the app is concerned.
 */
class NoOpAnalyticsTracker : AnalyticsTracker {
    override fun logScreenView(screenName: String) = Unit

    override fun logEvent(
        name: String,
        params: Map<String, Any?>,
    ) = Unit

    override fun setUserProperty(
        name: String,
        value: String?,
    ) = Unit

    override fun setCollectionEnabled(enabled: Boolean) = Unit
}
