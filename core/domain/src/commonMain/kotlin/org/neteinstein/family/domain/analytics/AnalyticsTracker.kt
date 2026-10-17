package org.neteinstein.family.domain.analytics

/**
 * The app's only telemetry seam. Declared here (rather than in `core:data`) so `feature:*`, `app`
 * and `core:ui` can report events without depending on `core:data`, per this project's module
 * boundary rules - the real implementations live in `core:data`, one per platform, bound through
 * `platformAnalyticsModule`.
 *
 * Implementations must never throw: analytics is strictly best-effort, and a misconfigured or
 * absent Firebase project (a clean checkout has no `google-services.json`, and a web build with no
 * `firebase-web-config.json` emits no init script) must degrade to a silent no-op rather than
 * taking a screen down with it.
 *
 * **No user identifier is set anywhere in this app.** There is no account, no sign-in and no
 * generated install ID; per-user metrics come from Firebase's own app-instance ID. What
 * [setUserProperty] carries is coarse segmentation only - see [AnalyticsUserProperties].
 */
interface AnalyticsTracker {
    /**
     * Reports a screen view. [screenName] is one of [AnalyticsScreens]'s constants, not a raw
     * navigation route - the two coincide for real destinations but the grid and full-screen card
     * are local UI state rather than routes (see feature:home's HomeScreen).
     */
    fun logScreenView(screenName: String)

    /**
     * Reports one event. [name] must come from [AnalyticsEvents] and [params]' keys from
     * [AnalyticsParams] - the `AnalyticsEventsTest` in core:domain enforces Firebase's naming
     * rules over those constants so a typo fails the build rather than silently dropping events
     * in production.
     *
     * Values are `Any?` rather than `String` so numbers stay real Firebase *metrics* (summable,
     * averageable) instead of string dimensions. Each implementation handles `String`, `Int`,
     * `Long`, `Double` and `Boolean`; anything else - `null` included - is dropped silently.
     */
    fun logEvent(
        name: String,
        params: Map<String, Any?> = emptyMap(),
    )

    /** Sets a segmentation dimension from [AnalyticsUserProperties]; `null` clears it. */
    fun setUserProperty(
        name: String,
        value: String?,
    )

    /**
     * Turns collection on or off at the SDK level, so the SDK itself stops buffering and
     * uploading rather than merely having its calls dropped upstream. Driven by the Settings
     * screen's "Share usage data" switch through `ConsentAwareAnalyticsTracker`.
     */
    fun setCollectionEnabled(enabled: Boolean)
}
