package org.neteinstein.family.data.analytics

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import org.neteinstein.family.domain.analytics.AnalyticsEvents
import org.neteinstein.family.domain.analytics.AnalyticsTracker
import org.neteinstein.family.domain.repository.AnalyticsPreferenceRepository
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** Records what reached the SDK, in order, so ordering around the consent switch can be asserted. */
private class RecordingTracker : AnalyticsTracker {
    val calls = mutableListOf<String>()

    override fun logScreenView(screenName: String) {
        calls += "screen:$screenName"
    }

    override fun logEvent(
        name: String,
        params: Map<String, Any?>,
    ) {
        calls += "event:$name"
    }

    override fun setUserProperty(
        name: String,
        value: String?,
    ) {
        calls += "property:$name=$value"
    }

    override fun setCollectionEnabled(enabled: Boolean) {
        calls += "collection:$enabled"
    }
}

private class FakePreferences(
    initial: Boolean,
) : AnalyticsPreferenceRepository {
    private val state = MutableStateFlow(initial)

    override val analyticsEnabled: Flow<Boolean> = state

    override fun isAnalyticsEnabled(): Boolean = state.value

    override suspend fun setAnalyticsEnabled(enabled: Boolean) {
        state.value = enabled
    }
}

class ConsentAwareAnalyticsTrackerTest {
    private val delegate = RecordingTracker()

    private fun tracker(enabled: Boolean) = ConsentAwareAnalyticsTracker(delegate, FakePreferences(enabled))

    @Test
    fun `syncs the SDK to the stored choice on construction`() {
        tracker(enabled = false)

        assertEquals(listOf("collection:false"), delegate.calls)
    }

    @Test
    fun `forwards everything while opted in`() {
        val tracker = tracker(enabled = true)
        delegate.calls.clear()

        tracker.logEvent("some_event")
        tracker.logScreenView("home")
        tracker.setUserProperty("theme_mode", "dark")

        assertEquals(listOf("event:some_event", "screen:home", "property:theme_mode=dark"), delegate.calls)
    }

    @Test
    fun `drops everything while opted out`() {
        val tracker = tracker(enabled = false)
        delegate.calls.clear()

        tracker.logEvent("some_event")
        tracker.logScreenView("home")
        tracker.setUserProperty("theme_mode", "dark")

        assertTrue(delegate.calls.isEmpty(), "expected no calls to reach the SDK, got ${delegate.calls}")
    }

    /**
     * The ordering is the point: logged after collection stopped, the opt-out event would never
     * leave the device, so the one number telling you how under-represented the data is would be
     * the one number you can't measure.
     */
    @Test
    fun `opting out logs the opt-out event before disabling collection`() {
        val tracker = tracker(enabled = true)
        delegate.calls.clear()

        tracker.setCollectionEnabled(false)

        assertEquals(
            listOf("event:${AnalyticsEvents.ANALYTICS_OPT_OUT}", "collection:false"),
            delegate.calls,
        )
    }

    /** The mirror image: an opt-in event logged before collection restarts would also be dropped. */
    @Test
    fun `opting in enables collection before logging the opt-in event`() {
        val tracker = tracker(enabled = false)
        delegate.calls.clear()

        tracker.setCollectionEnabled(true)

        assertEquals(
            listOf("collection:true", "event:${AnalyticsEvents.ANALYTICS_OPT_IN}"),
            delegate.calls,
        )
    }
}
