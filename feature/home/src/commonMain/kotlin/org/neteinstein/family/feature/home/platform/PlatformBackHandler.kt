package org.neteinstein.family.feature.home.platform

import androidx.compose.runtime.Composable

/**
 * Intercepts the system back gesture/button while [enabled], calling [onBack] instead of the
 * default back navigation. Android-only (androidx.activity's BackHandler is tied to
 * ComponentActivity) - a no-op on iOS/Web, which have no equivalent system back gesture to
 * intercept here; HomeScreen's full-screen question overlay simply has no other way to close on
 * those platforms yet (its own close button still works).
 */
@Composable
expect fun PlatformBackHandler(
    enabled: Boolean,
    onBack: () -> Unit,
)
