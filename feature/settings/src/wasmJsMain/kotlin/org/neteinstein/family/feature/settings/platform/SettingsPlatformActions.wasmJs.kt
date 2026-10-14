package org.neteinstein.family.feature.settings.platform

import androidx.compose.runtime.Composable

@Composable
actual fun rememberOpenLanguageSettingsAction(): (() -> Unit)? = null

// The web build has no installed-package version to read (unlike Android's PackageManager or
// iOS's NSBundle) and isn't part of the "1.0.<run number>" release train release.yml computes for
// the Android APK/AAB - it's deployed straight from main by deploy-pages.yml on every push. Static
// for now, matching androidApp/build.gradle.kts's own fallback default so it's at least a real,
// meaningful version rather than a "—" placeholder; bump it by hand alongside notable web releases
// until this gets real deploy-time version injection.
private const val WEB_VERSION_NAME = "1.0.0"

@Composable
actual fun rememberCurrentVersionName(): String = WEB_VERSION_NAME
