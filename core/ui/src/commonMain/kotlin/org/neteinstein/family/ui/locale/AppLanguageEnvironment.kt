// Compose Multiplatform has no public API for overriding the language its resource lookups
// (`stringResource`, `painterResource`, ...) resolve against - they always follow
// `androidx.compose.ui.text.intl.Locale.current`, i.e. the OS/browser locale
// (CMP-2870/compose-multiplatform#4347). The one seam that exists, the `LocalComposeEnvironment`
// CompositionLocal every resource getter reads, is `internal` to compose's `components-resources`
// module, so reaching it needs these suppressions. Everything else here is public-but-opt-in
// (`@InternalResourceApi`). Verified against Compose Multiplatform 1.11.1 for the android, iosX,
// and wasmJs targets; if a CMP bump breaks this file, `AppLanguageEnvironmentTest` fails and the
// fix is to re-check `ResourceEnvironment.kt` in that version's `components-resources` sources.
@file:Suppress("INVISIBLE_REFERENCE", "INVISIBLE_MEMBER")

package org.neteinstein.family.ui.locale

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalDensity
import org.jetbrains.compose.resources.ComposeEnvironment
import org.jetbrains.compose.resources.DensityQualifier
import org.jetbrains.compose.resources.InternalResourceApi
import org.jetbrains.compose.resources.LanguageQualifier
import org.jetbrains.compose.resources.LocalComposeEnvironment
import org.jetbrains.compose.resources.RegionQualifier
import org.jetbrains.compose.resources.ResourceEnvironment
import org.jetbrains.compose.resources.ThemeQualifier

/**
 * Makes every `stringResource`/`painterResource` call under [content] resolve against
 * [languageCode] (an ISO 639-1 code, no region subtag) instead of the OS/browser locale, so the
 * app's own UI follows the user's in-app language choice and not just the question cards.
 *
 * [languageCode] `null` means "no override": composition is left untouched, so resources keep
 * resolving against the platform locale exactly as they do without this wrapper. Only the language
 * is overridden - theme and density qualifiers still come from the platform, and the region
 * qualifier is cleared (the app ships no region-specific resources, and a region inherited from a
 * locale the user has just overridden away from would be wrong).
 */
@Composable
fun ProvideAppLanguage(
    languageCode: String?,
    content: @Composable () -> Unit,
) {
    if (languageCode == null) {
        content()
        return
    }
    val composeEnvironment = remember(languageCode) { AppLanguageComposeEnvironment(languageCode) }
    CompositionLocalProvider(LocalComposeEnvironment provides composeEnvironment, content = content)
}

/**
 * Drop-in replacement for compose-resources' own `DefaultComposeEnvironment` that pins the
 * language qualifier and takes everything else from the platform, the same way the default does.
 */
private class AppLanguageComposeEnvironment(
    private val languageCode: String,
) : ComposeEnvironment {
    @OptIn(InternalResourceApi::class)
    @Composable
    override fun rememberEnvironment(): ResourceEnvironment {
        val isDarkTheme = isSystemInDarkTheme()
        val density = LocalDensity.current
        return remember(languageCode, isDarkTheme, density) {
            ResourceEnvironment(
                language = LanguageQualifier(languageCode),
                region = RegionQualifier(""),
                theme = ThemeQualifier.selectByValue(isDarkTheme),
                density = DensityQualifier.selectByDensity(density.density),
            )
        }
    }
}
