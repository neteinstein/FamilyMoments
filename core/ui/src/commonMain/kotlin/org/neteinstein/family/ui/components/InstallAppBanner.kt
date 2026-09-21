package org.neteinstein.family.ui.components

import androidx.compose.runtime.Composable

/**
 * A dismissible banner prompting the user to install the native Android app - shown only on the
 * web (wasmJs) build *when the browser's user agent is Android* (installing an Android APK makes
 * no sense from desktop/iOS), where it links out to this project's GitHub Releases page (the app
 * has no app-store presence there; see AGENTS.md's `github` update flavor for the same
 * distribution channel used by the in-app updater). A no-op on Android/iOS, which are already the
 * native app. The dismissal choice is remembered in the browser's `localStorage` so it doesn't
 * reappear on the next visit.
 *
 * [onBannerAction] reports what the user did with the banner - `shown`, `clicked` or `dismissed`,
 * the values of `AnalyticsParams.ACTION`. It is a callback rather than an injected tracker because
 * `core:ui` deliberately depends on nothing but Compose (see AGENTS.md's module dependency rules);
 * `app`'s `App()` supplies one that forwards to the real `AnalyticsTracker`.
 */
@Composable
expect fun InstallAppBanner(onBannerAction: (String) -> Unit)
