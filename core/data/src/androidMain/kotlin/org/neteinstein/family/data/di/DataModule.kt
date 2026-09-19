package org.neteinstein.family.data.di

import org.koin.core.module.Module
import org.koin.dsl.module
import org.neteinstein.family.data.installer.AppUpdateInstallerImpl
import org.neteinstein.family.data.repository.GitHubUpdateRepositoryImpl
import org.neteinstein.family.domain.repository.AppUpdateInstaller
import org.neteinstein.family.domain.repository.UpdateRepository
import org.neteinstein.family.domain.usecase.CheckForUpdateUseCase
import org.neteinstein.family.domain.usecase.ClearDownloadedUpdateUseCase
import org.neteinstein.family.domain.usecase.DownloadAppUpdateUseCase

actual val platformUpdateModule: Module =
    module {
        single<UpdateRepository> { GitHubUpdateRepositoryImpl() }
        single<AppUpdateInstaller> { AppUpdateInstallerImpl() }
        factory { CheckForUpdateUseCase(get()) }
        factory { DownloadAppUpdateUseCase(get()) }
        factory { ClearDownloadedUpdateUseCase(get()) }
    }
