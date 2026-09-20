package org.neteinstein.family.feature.settings.platform

import androidx.compose.runtime.Composable

/**
 * Opens the OS's language-selection settings for this app. Android-only (there's a system
 * per-app language settings page to deep-link into) - `null` on iOS/Web, which have no
 * equivalent, so SettingsScreen hides the language item there entirely rather than showing a
 * control that does nothing when tapped.
 */
@Composable
expect fun rememberOpenLanguageSettingsAction(): (() -> Unit)?

/** The installed build's version name (e.g. "1.2.3"), as shown in the "About" section. */
@Composable
expect fun rememberCurrentVersionName(): String
