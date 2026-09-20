package org.neteinstein.family.data.di

import org.koin.core.module.Module
import org.koin.dsl.module
import org.neteinstein.family.data.installer.NoOpAppUpdateInstaller
import org.neteinstein.family.data.repository.NoOpUpdateRepository
import org.neteinstein.family.domain.repository.AppUpdateInstaller
import org.neteinstein.family.domain.repository.UpdateRepository

// The GitHub self-update feature is Android-only (APK sideloading has no iOS equivalent) - these
// no-op bindings only exist so Koin can satisfy injection; see NoOpUpdateRepository's kdoc.
actual val platformUpdateModule: Module =
    module {
        single<UpdateRepository> { NoOpUpdateRepository() }
        single<AppUpdateInstaller> { NoOpAppUpdateInstaller() }
    }
