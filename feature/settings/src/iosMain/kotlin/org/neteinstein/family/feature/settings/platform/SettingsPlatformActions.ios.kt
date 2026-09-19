package org.neteinstein.family.feature.settings.platform

import androidx.compose.runtime.Composable
import platform.Foundation.NSBundle

@Composable
actual fun rememberOpenLanguageSettingsAction(): () -> Unit = {}

@Composable
actual fun rememberCurrentVersionName(): String {
    val versionKey = "CFBundleShortVersionString"
    return (NSBundle.mainBundle.infoDictionary?.get(versionKey) as? String) ?: "—"
}
