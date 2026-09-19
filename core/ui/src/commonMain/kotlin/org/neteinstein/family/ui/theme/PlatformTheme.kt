package org.neteinstein.family.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable

/**
 * A platform "dynamic color" scheme (Android 12+/API 31+ Material You) if [dynamicColor] is true
 * and the platform supports it, else null - callers fall back to the static light/dark schemes.
 * Always null on iOS/Web, which have no equivalent concept.
 */
@Composable
expect fun platformDynamicColorScheme(
    darkTheme: Boolean,
    dynamicColor: Boolean,
): ColorScheme?

/**
 * Adjusts platform chrome to match [darkTheme] - Android's status bar icon appearance. A no-op on
 * iOS/Web, which have no equivalent concept here.
 */
@Composable
expect fun PlatformStatusBarEffect(darkTheme: Boolean)
