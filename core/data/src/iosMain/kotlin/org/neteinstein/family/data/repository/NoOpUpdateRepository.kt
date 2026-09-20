package org.neteinstein.family.data.repository

import org.neteinstein.family.domain.model.AppUpdate
import org.neteinstein.family.domain.model.PlatformFile
import org.neteinstein.family.domain.model.UpdateCheckResult
import org.neteinstein.family.domain.repository.UpdateRepository

/**
 * The GitHub self-update feature is Android-only (APK sideloading has no iOS equivalent) - this
 * exists purely so Koin can satisfy [UpdateRepository] injection on iOS. `feature:settings` never
 * actually calls it: its "Updates" section is hidden whenever the `updatesEnabled` DI flag is
 * false, which it always is off Android.
 */
class NoOpUpdateRepository : UpdateRepository {
    override suspend fun checkForUpdate(): Result<UpdateCheckResult> =
        Result.failure(UnsupportedOperationException("Self-update is not supported on this platform"))

    override suspend fun downloadUpdate(update: AppUpdate): Result<PlatformFile> =
        Result.failure(UnsupportedOperationException("Self-update is not supported on this platform"))

    override suspend fun clearDownloadedUpdate(): Result<Unit> = Result.success(Unit)
}
