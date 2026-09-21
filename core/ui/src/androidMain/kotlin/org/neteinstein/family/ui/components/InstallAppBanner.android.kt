package org.neteinstein.family.ui.components

import androidx.compose.runtime.Composable

// No banner on the native apps - they are what the banner points at. onBannerAction is
// therefore never invoked here.
@Composable
actual fun InstallAppBanner(onBannerAction: (String) -> Unit) {
}
