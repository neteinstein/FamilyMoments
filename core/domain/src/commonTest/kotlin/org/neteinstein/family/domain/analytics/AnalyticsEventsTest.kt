package org.neteinstein.family.domain.analytics

import org.neteinstein.family.domain.model.QuestionCategory
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Firebase silently drops an event or parameter whose name breaks its rules - no exception, no log,
 * it just never appears in the console. These assertions turn that into a build failure instead.
 *
 * The rules (from Firebase's `logEvent`/`setUserProperty` documentation): names are at most 40
 * characters, contain only letters, digits and underscores, must start with a letter, and must not
 * begin with the reserved `firebase_`, `google_` or `ga_` prefixes.
 */
class AnalyticsEventsTest {
    private val eventNames =
        listOf(
            AnalyticsEvents.QUESTION_VIEWED,
            AnalyticsEvents.DECK_NAVIGATED,
            AnalyticsEvents.QUESTION_HIDDEN,
            AnalyticsEvents.CATEGORY_SELECTED,
            AnalyticsEvents.SHUFFLE_USED,
            AnalyticsEvents.VIEW_MODE_CHANGED,
            AnalyticsEvents.QUESTION_EXPANDED,
            AnalyticsEvents.THEME_CHANGED,
            AnalyticsEvents.LANGUAGE_CHANGED,
            AnalyticsEvents.CARDS_RESET,
            AnalyticsEvents.UPDATE_CHECK,
            AnalyticsEvents.UPDATE_DOWNLOAD_STARTED,
            AnalyticsEvents.OUTBOUND_LINK_CLICKED,
            AnalyticsEvents.INSTALL_BANNER,
            AnalyticsEvents.ANALYTICS_OPT_OUT,
            AnalyticsEvents.ANALYTICS_OPT_IN,
        )

    private val paramNames =
        listOf(
            AnalyticsParams.QUESTION_ID,
            AnalyticsParams.CATEGORY,
            AnalyticsParams.LANGUAGE,
            AnalyticsParams.DECK_POSITION,
            AnalyticsParams.DECK_SIZE,
            AnalyticsParams.SOURCE,
            AnalyticsParams.DIRECTION,
            AnalyticsParams.INPUT,
            AnalyticsParams.MODE,
            AnalyticsParams.THEME_MODE,
            AnalyticsParams.HIDDEN_TOTAL,
            AnalyticsParams.HIDDEN_COUNT_BEFORE,
            AnalyticsParams.RESULT,
            AnalyticsParams.TO_VERSION,
            AnalyticsParams.LINK,
            AnalyticsParams.ACTION,
            AnalyticsParams.SCREEN_NAME,
        )

    private val userPropertyNames =
        listOf(
            AnalyticsUserProperties.CONTENT_LANGUAGE,
            AnalyticsUserProperties.APP_LANGUAGE_OVERRIDE,
            AnalyticsUserProperties.THEME_MODE,
            AnalyticsUserProperties.PLATFORM,
            AnalyticsUserProperties.DISTRIBUTION,
            AnalyticsUserProperties.HIDDEN_CARDS_BUCKET,
        )

    private val screenNames =
        listOf(
            AnalyticsScreens.SPLASH,
            AnalyticsScreens.HOME,
            AnalyticsScreens.SETTINGS,
            AnalyticsScreens.HOME_GRID,
            AnalyticsScreens.QUESTION_FULLSCREEN,
        )

    @Test
    fun `every event name satisfies Firebase's naming rules`() {
        eventNames.forEach { assertValidName(it) }
    }

    @Test
    fun `every parameter name satisfies Firebase's naming rules`() {
        paramNames.forEach { assertValidName(it) }
    }

    @Test
    fun `every user property name satisfies Firebase's naming rules`() {
        userPropertyNames.forEach { assertValidName(it) }
    }

    @Test
    fun `every screen name satisfies Firebase's naming rules`() {
        screenNames.forEach { assertValidName(it) }
    }

    /** A duplicate would mean two different things reporting into the same series in the console. */
    @Test
    fun `names are unique within each catalog`() {
        assertEquals(eventNames.size, eventNames.toSet().size, "duplicate event name")
        assertEquals(paramNames.size, paramNames.toSet().size, "duplicate parameter name")
        assertEquals(userPropertyNames.size, userPropertyNames.toSet().size, "duplicate user property name")
        assertEquals(screenNames.size, screenNames.toSet().size, "duplicate screen name")
    }

    @Test
    fun `every category maps to a distinct analytics name`() {
        val names = QuestionCategory.all.map { it.analyticsName() }

        assertEquals(QuestionCategory.all.size, names.toSet().size, "duplicate category name in $names")
        names.forEach { assertValidName(it) }
    }

    @Test
    fun `a null category reports as the all filter`() {
        assertEquals(ANALYTICS_VALUE_ALL, (null as QuestionCategory?).analyticsName())
    }

    /**
     * The buckets exist so the property stays a segmentation dimension rather than a near-unique
     * per-device number - the boundaries are asserted so a refactor can't quietly widen them into
     * something identifying.
     */
    @Test
    fun `hidden card counts bucket at the documented boundaries`() {
        assertEquals("0", bucketHiddenCards(0))
        assertEquals("0", bucketHiddenCards(-1))
        assertEquals("1_10", bucketHiddenCards(1))
        assertEquals("1_10", bucketHiddenCards(10))
        assertEquals("11_50", bucketHiddenCards(11))
        assertEquals("11_50", bucketHiddenCards(50))
        assertEquals("51_plus", bucketHiddenCards(51))
        assertEquals("51_plus", bucketHiddenCards(5_000))
    }

    /**
     * core:ui can't import this catalog (it depends on nothing but Compose - see AGENTS.md's
     * module dependency rules), so InstallAppBanner.wasmJs.kt repeats these three strings as
     * private constants. If they ever diverge, the banner's events land under values nothing in
     * the console is grouped by.
     */
    @Test
    fun `install banner action values match the ones core-ui hardcodes`() {
        // The right-hand literals are exactly what InstallAppBanner.wasmJs.kt declares privately.
        // Renaming a constant on the left without updating that file fails here.
        assertEquals("shown", AnalyticsBannerActions.SHOWN)
        assertEquals("clicked", AnalyticsBannerActions.CLICKED)
        assertEquals("dismissed", AnalyticsBannerActions.DISMISSED)

        listOf(
            AnalyticsBannerActions.SHOWN,
            AnalyticsBannerActions.CLICKED,
            AnalyticsBannerActions.DISMISSED,
        ).forEach { assertValidName(it) }
    }

    private fun assertValidName(name: String) {
        assertTrue(name.isNotEmpty(), "name is empty")
        assertTrue(name.length <= MAX_NAME_LENGTH, "'$name' is ${name.length} chars, over Firebase's $MAX_NAME_LENGTH")
        assertTrue(name.first().isLetter(), "'$name' must start with a letter")
        assertTrue(
            name.all { it.isLetterOrDigit() || it == '_' },
            "'$name' may only contain letters, digits and underscores",
        )
        assertTrue(
            RESERVED_PREFIXES.none { name.startsWith(it) },
            "'$name' starts with a prefix Firebase reserves: $RESERVED_PREFIXES",
        )
    }

    private companion object {
        const val MAX_NAME_LENGTH = 40
        val RESERVED_PREFIXES = listOf("firebase_", "google_", "ga_")
    }
}
