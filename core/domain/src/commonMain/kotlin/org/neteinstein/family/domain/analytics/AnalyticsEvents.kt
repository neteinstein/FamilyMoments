package org.neteinstein.family.domain.analytics

import org.neteinstein.family.domain.model.QuestionCategory

/**
 * Every analytics name the app reports, in one place, so a name can't drift between the site that
 * logs it and the Firebase console dashboard built on it. Renaming a constant here starts a new
 * series in Firebase and orphans the old one - treat these strings as a published schema.
 *
 * Firebase's limits, enforced by `AnalyticsEventsTest`: event and parameter names are at most 40
 * characters of `[a-z0-9_]` starting with a letter, may not begin with the reserved `firebase_`/
 * `google_`/`ga_` prefixes, and string parameter values are truncated past 100 characters. An
 * event carries at most 25 parameters; none here comes close.
 */
object AnalyticsEvents {
    /** A question was shown to the user, however it got on screen - see [AnalyticsParams.SOURCE]. */
    const val QUESTION_VIEWED = "question_viewed"

    /** The user moved through the deck - see [AnalyticsParams.DIRECTION]/[AnalyticsParams.INPUT]. */
    const val DECK_NAVIGATED = "deck_navigated"

    /**
     * A card was hidden (swipe down, confirmed). Grouped by [AnalyticsParams.QUESTION_ID] this is
     * the most useful content signal the app produces: the questions people hide most are the
     * ones worth rewriting or dropping from QuestionSeedData.
     */
    const val QUESTION_HIDDEN = "question_hidden"

    /** The category filter changed, "All" included - see [AnalyticsParams.CATEGORY]. */
    const val CATEGORY_SELECTED = "category_selected"

    const val SHUFFLE_USED = "shuffle_used"

    /** Grid/swipe toggle - see [AnalyticsParams.MODE]. */
    const val VIEW_MODE_CHANGED = "view_mode_changed"

    /** A card was opened full screen - see [AnalyticsParams.SOURCE]. */
    const val QUESTION_EXPANDED = "question_expanded"

    const val THEME_CHANGED = "theme_changed"

    const val LANGUAGE_CHANGED = "language_changed"

    const val CARDS_RESET = "cards_reset"

    /** Outcome of the "Update to latest" check - see [AnalyticsParams.RESULT]. "github" flavor only. */
    const val UPDATE_CHECK = "update_check"

    const val UPDATE_DOWNLOAD_STARTED = "update_download_started"

    const val OUTBOUND_LINK_CLICKED = "outbound_link_clicked"

    /** Web-only "install the Android app" banner - see [AnalyticsParams.ACTION]. */
    const val INSTALL_BANNER = "install_banner"

    /**
     * Logged *before* collection is disabled - after it, nothing would ever leave the device.
     * Its volume is what tells you how much the rest of the data under-represents.
     */
    const val ANALYTICS_OPT_OUT = "analytics_opt_out"

    /** Logged *after* collection is re-enabled, for the same reason in reverse. */
    const val ANALYTICS_OPT_IN = "analytics_opt_in"
}

/** Parameter names for [AnalyticsEvents]. See that object's kdoc for Firebase's naming limits. */
object AnalyticsParams {
    const val QUESTION_ID = "question_id"
    const val CATEGORY = "category"
    const val LANGUAGE = "language"
    const val DECK_POSITION = "deck_position"
    const val DECK_SIZE = "deck_size"

    /** Where a question came from: `deck`, `grid`, `shuffle`, `swipe_up`, `fullscreen_random`. */
    const val SOURCE = "source"

    /** `next` or `previous`. */
    const val DIRECTION = "direction"

    /** `swipe` or `keyboard` - the latter is effectively Web-only. */
    const val INPUT = "input"

    /** `grid` or `swipe`. */
    const val MODE = "mode"

    const val THEME_MODE = "theme_mode"
    const val HIDDEN_TOTAL = "hidden_total"
    const val HIDDEN_COUNT_BEFORE = "hidden_count_before"

    /** `up_to_date`, `available`, `failed` or `blocked`. */
    const val RESULT = "result"

    const val TO_VERSION = "to_version"

    /** `loopgain`, `pedrovicente` or `github_releases`. */
    const val LINK = "link"

    /** `shown`, `clicked` or `dismissed`. */
    const val ACTION = "action"

    /** Firebase's own reserved screen-view parameter name, used by [AnalyticsTracker.logScreenView]. */
    const val SCREEN_NAME = "screen_name"
}

/**
 * Screen names reported by [AnalyticsTracker.logScreenView]. The first three match the navigation
 * routes in `app`'s `Screen.kt`; the last two are not destinations at all but local UI state in
 * feature:home's HomeScreen, tracked as screens because that is how they read to a user.
 */
object AnalyticsScreens {
    const val SPLASH = "splash"
    const val HOME = "home"
    const val SETTINGS = "settings"
    const val HOME_GRID = "home_grid"
    const val QUESTION_FULLSCREEN = "question_fullscreen"
}

/**
 * Coarse segmentation dimensions. Deliberately no identifier of any kind: there is no account and
 * no generated install ID in this app, and [HIDDEN_CARDS_BUCKET] is bucketed rather than a raw
 * count precisely so it stays a dimension instead of a near-unique per-device value.
 *
 * Firebase allows 25 custom user properties per project; these use six.
 */
object AnalyticsUserProperties {
    /** Language the question deck is being read in (`en`/`pt`/`es`/`fr`/`de`). */
    const val CONTENT_LANGUAGE = "content_language"

    /** The in-app language override, or `automatic` when following the OS/browser locale. */
    const val APP_LANGUAGE_OVERRIDE = "app_language_override"

    const val THEME_MODE = "theme_mode"

    /** `android` or `web`. */
    const val PLATFORM = "platform"

    /** `github` or `playstore` on Android; unset elsewhere. */
    const val DISTRIBUTION = "distribution"

    /** `0`, `1_10`, `11_50` or `51_plus` - see [bucketHiddenCards]. */
    const val HIDDEN_CARDS_BUCKET = "hidden_cards_bucket"
}

/**
 * Values of [AnalyticsParams.ACTION] for [AnalyticsEvents.INSTALL_BANNER].
 *
 * `core:ui`, which raises these events, cannot import this catalog - it depends on nothing but
 * Compose, per AGENTS.md's module dependency rules - so `InstallAppBanner.wasmJs.kt` repeats the
 * three strings as private constants of its own. This object is the canonical spelling, and
 * `AnalyticsEventsTest` pins the literals so a rename here can't silently drift from the copy
 * there.
 */
object AnalyticsBannerActions {
    const val SHOWN = "shown"
    const val CLICKED = "clicked"
    const val DISMISSED = "dismissed"
}

/** Value used when the user has expressed no preference and the app follows the OS/browser. */
const val ANALYTICS_VALUE_AUTOMATIC = "automatic"

/** Value used for the "All categories" filter, which is modelled as a null category. */
const val ANALYTICS_VALUE_ALL = "all"

/**
 * Buckets a hidden-card count into [AnalyticsUserProperties.HIDDEN_CARDS_BUCKET]. Reporting the
 * raw number would make the property near-unique per device, which is both useless as a
 * segmentation dimension and needlessly identifying.
 */
fun bucketHiddenCards(count: Int): String =
    when {
        count <= 0 -> "0"
        count <= 10 -> "1_10"
        count <= 50 -> "11_50"
        else -> "51_plus"
    }

/**
 * Analytics name for a category, with `null` - the "All" filter - mapped to [ANALYTICS_VALUE_ALL].
 *
 * [QuestionCategory] is a sealed class rather than an enum, so there is no `name` to lowercase;
 * spelling the mapping out has the better property anyway - the `when` is exhaustive, so adding a
 * category without deciding its analytics name is a compile error rather than a silently missing
 * dimension value.
 */
fun QuestionCategory?.analyticsName(): String =
    when (this) {
        null -> ANALYTICS_VALUE_ALL
        QuestionCategory.IceBreakers -> "ice_breakers"
        QuestionCategory.Memories -> "memories"
        QuestionCategory.Values -> "values"
        QuestionCategory.FutureDreams -> "future_dreams"
        QuestionCategory.DailyLife -> "daily_life"
    }
