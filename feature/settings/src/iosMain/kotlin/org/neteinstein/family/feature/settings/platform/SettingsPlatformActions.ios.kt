package org.neteinstein.family.feature.settings.platform

import androidx.compose.runtime.Composable
import platform.Foundation.NSBundle

@Composable
actual fun rememberOpenLanguageSettingsAction(): () -> Unit = {}

@Composable
actual fun rememberCurrentVersionName(): String =
    (NSBundle.mainBundle.infoDictionary?.get("CFBundleShortVersionString") as? String) ?: "—"
