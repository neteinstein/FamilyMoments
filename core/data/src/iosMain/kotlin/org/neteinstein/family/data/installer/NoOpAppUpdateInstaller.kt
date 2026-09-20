package org.neteinstein.family.data.installer

import org.neteinstein.family.domain.model.PlatformFile
import org.neteinstein.family.domain.repository.AppUpdateInstaller

/**
 * The GitHub self-update feature is Android-only (APK sideloading has no iOS equivalent) - this
 * exists purely so Koin can satisfy [AppUpdateInstaller] injection on iOS. `feature:settings`
 * never actually calls it: its "Updates" section is hidden whenever the `updatesEnabled` DI flag
 * is false, which it always is off Android.
 */
class NoOpAppUpdateInstaller : AppUpdateInstaller {
    override fun canInstallPackages(): Boolean = false

    override fun openInstallPermissionSettings() = Unit

    override fun installPackage(apkFile: PlatformFile) = Unit
}
