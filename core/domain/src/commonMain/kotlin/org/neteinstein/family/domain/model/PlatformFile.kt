package org.neteinstein.family.domain.model

/**
 * Opaque handle to a downloaded update APK sitting in local storage. The GitHub self-update
 * feature (see [org.neteinstein.family.domain.repository.UpdateRepository]) is Android-only - APK
 * sideloading has no iOS/Web equivalent - so this is only ever actually created and consumed on
 * Android, where it's a `java.io.File`. iOS/Web never produce one: their [UpdateRepository]/
 * [org.neteinstein.family.domain.repository.AppUpdateInstaller] implementations always fail or
 * no-op before a [PlatformFile] would be needed.
 */
expect class PlatformFile
