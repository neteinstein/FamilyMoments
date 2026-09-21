package org.neteinstein.family.data.analytics

import org.neteinstein.family.domain.analytics.AnalyticsParams
import org.neteinstein.family.domain.analytics.AnalyticsTracker

/**
 * Firebase Analytics on the Web, over the `window.__familyMomentsAnalytics` bridge that
 * `webApp`'s generated `firebase-init.js` publishes.
 *
 * The Firebase JS SDK is reached through that bridge rather than as a Kotlin dependency because
 * Kotlin/Wasm has no `@JsModule` support and this repo has no npm/webpack setup to hang one off -
 * `webApp` is a plain `wasmJs { browser() }` target with four source files. `firebase-init.js`
 * imports the SDK as an ES module from the gstatic CDN and exposes a four-method object; see
 * `webApp/firebase-init.js.template` for the contract, including the call queue that covers the
 * window between this code starting and the deferred module finishing its import.
 *
 * The bridge is absent entirely when the build had no `firebase-web-config.json`/
 * `FIREBASE_WEB_CONFIG` to render (a clean checkout, or a fork's CI), in which case
 * [bridgeAvailable] stays false and every method no-ops - see [AnalyticsTracker]'s kdoc.
 */
class FirebaseWebAnalyticsTracker : AnalyticsTracker {
    override fun logScreenView(screenName: String) {
        logEvent("screen_view", mapOf(AnalyticsParams.SCREEN_NAME to screenName))
    }

    override fun logEvent(
        name: String,
        params: Map<String, Any?>,
    ) {
        if (!bridgeAvailable()) return
        val jsParams = newJsObject()
        params.forEach { (key, value) ->
            // Mirrors the Android actual's Bundle mapping: numbers stay numbers so they remain
            // summable metrics in the console, and anything else is dropped rather than
            // stringified into a plausible-looking wrong value.
            when (value) {
                is String -> jsPutString(jsParams, key, value)
                is Int -> jsPutNumber(jsParams, key, value.toDouble())
                is Long -> jsPutNumber(jsParams, key, value.toDouble())
                is Double -> jsPutNumber(jsParams, key, value)
                is Float -> jsPutNumber(jsParams, key, value.toDouble())
                is Boolean -> jsPutString(jsParams, key, value.toString())
                else -> Unit
            }
        }
        jsLogEvent(name, jsParams)
    }

    override fun setUserProperty(
        name: String,
        value: String?,
    ) {
        if (!bridgeAvailable()) return
        val properties = newJsObject()
        // The modular SDK's setUserProperties takes an object; a null value clears the property.
        if (value == null) jsPutNull(properties, name) else jsPutString(properties, name, value)
        jsSetUserProperties(properties)
    }

    override fun setCollectionEnabled(enabled: Boolean) {
        if (!bridgeAvailable()) return
        jsSetCollectionEnabled(enabled)
    }
}

private fun bridgeAvailable(): Boolean = js("typeof window !== 'undefined' && window.__familyMomentsAnalytics != null")

private fun newJsObject(): JsAny = js("({})")

private fun jsPutString(
    target: JsAny,
    key: String,
    value: String,
) {
    js("target[key] = value")
}

private fun jsPutNumber(
    target: JsAny,
    key: String,
    value: Double,
) {
    js("target[key] = value")
}

private fun jsPutNull(
    target: JsAny,
    key: String,
) {
    js("target[key] = null")
}

private fun jsLogEvent(
    name: String,
    params: JsAny,
) {
    js("window.__familyMomentsAnalytics.logEvent(name, params)")
}

private fun jsSetUserProperties(properties: JsAny) {
    js("window.__familyMomentsAnalytics.setUserProperties(properties)")
}

private fun jsSetCollectionEnabled(enabled: Boolean) {
    js("window.__familyMomentsAnalytics.setCollectionEnabled(enabled)")
}
