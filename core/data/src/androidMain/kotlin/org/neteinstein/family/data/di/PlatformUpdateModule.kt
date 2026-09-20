package org.neteinstein.family.data.di

import org.koin.core.module.Module
import org.koin.dsl.module
import org.neteinstein.family.data.installer.AppUpdateInstallerImpl
import org.neteinstein.family.data.repository.GitHubUpdateRepositoryImpl
import org.neteinstein.family.domain.repository.AppUpdateInstaller
import org.neteinstein.family.domain.repository.UpdateRepository

// The use cases wrapping UpdateRepository/AppUpdateInstaller are common code (see
// core:domain's CheckForUpdateUseCase et al.) and registered once in the shared dataModule -
// only the concrete GitHub-releases-backed/Android-Context-backed implementations are
// platform-specific.
actual val platformUpdateModule: Module =
    module {
        single<UpdateRepository> { GitHubUpdateRepositoryImpl() }
        single<AppUpdateInstaller> { AppUpdateInstallerImpl() }
    }
