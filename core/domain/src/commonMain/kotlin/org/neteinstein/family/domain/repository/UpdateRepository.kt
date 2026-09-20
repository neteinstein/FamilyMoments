package org.neteinstein.family.domain.repository

import org.neteinstein.family.domain.model.AppUpdate
import org.neteinstein.family.domain.model.PlatformFile
import org.neteinstein.family.domain.model.UpdateCheckResult

/** Boundary between the update-check domain layer and GitHub Releases. */
interface UpdateRepository {
    /** Compares the installed build's version against GitHub's latest release. */
    suspend fun checkForUpdate(): Result<UpdateCheckResult>

    /** Downloads [update]'s APK to local storage, returning the file it was written to. */
    suspend fun downloadUpdate(update: AppUpdate): Result<PlatformFile>

    /**
     * Deletes any APK previously written by [downloadUpdate] - a no-op, not a failure, if
     * nothing was downloaded.
     */
    suspend fun clearDownloadedUpdate(): Result<Unit>
}
