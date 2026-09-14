package org.neteinstein.family.data.di

import androidx.room.Room
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module
import org.neteinstein.family.data.installer.AppUpdateInstallerImpl
import org.neteinstein.family.data.local.FamilyMomentsDatabase
import org.neteinstein.family.data.locale.LocaleProviderImpl
import org.neteinstein.family.data.preferences.ThemePreferenceRepositoryImpl
import org.neteinstein.family.data.repository.GitHubUpdateRepositoryImpl
import org.neteinstein.family.data.repository.QuestionRepositoryImpl
import org.neteinstein.family.data.repository.UsedQuestionsRepositoryImpl
import org.neteinstein.family.domain.repository.AppUpdateInstaller
import org.neteinstein.family.domain.repository.LocaleProvider
import org.neteinstein.family.domain.repository.QuestionRepository
import org.neteinstein.family.domain.repository.ThemePreferenceRepository
import org.neteinstein.family.domain.repository.UpdateRepository
import org.neteinstein.family.domain.repository.UsedQuestionsRepository
import org.neteinstein.family.domain.usecase.CheckForUpdateUseCase
import org.neteinstein.family.domain.usecase.ClearDownloadedUpdateUseCase
import org.neteinstein.family.domain.usecase.DownloadAppUpdateUseCase
import org.neteinstein.family.domain.usecase.GetQuestionsUseCase
import org.neteinstein.family.domain.usecase.GetRandomQuestionUseCase
import org.neteinstein.family.domain.usecase.GetThemeModeUseCase
import org.neteinstein.family.domain.usecase.GetUsedQuestionIdsUseCase
import org.neteinstein.family.domain.usecase.MarkQuestionUsedUseCase
import org.neteinstein.family.domain.usecase.ResetUsedQuestionsUseCase
import org.neteinstein.family.domain.usecase.SetThemeModeUseCase

val dataModule =
    module {
        single {
            Room
                .databaseBuilder(androidContext(), FamilyMomentsDatabase::class.java, "family_moments.db")
                .addMigrations(FamilyMomentsDatabase.MIGRATION_1_2)
                .build()
        }
        single { get<FamilyMomentsDatabase>().cardDao() }
        single { get<FamilyMomentsDatabase>().seedMetadataDao() }
        single<QuestionRepository> { QuestionRepositoryImpl(get(), get()) }
        single<LocaleProvider> { LocaleProviderImpl() }
        single<UsedQuestionsRepository> { UsedQuestionsRepositoryImpl(get()) }
        single<ThemePreferenceRepository> { ThemePreferenceRepositoryImpl(androidContext()) }
        factory { GetThemeModeUseCase(get()) }
        factory { SetThemeModeUseCase(get()) }
        factory { GetRandomQuestionUseCase(get()) }
        factory { GetQuestionsUseCase(get()) }
        factory { GetUsedQuestionIdsUseCase(get()) }
        factory { MarkQuestionUsedUseCase(get()) }
        factory { ResetUsedQuestionsUseCase(get()) }

        single<UpdateRepository> { GitHubUpdateRepositoryImpl(context = androidContext()) }
        single<AppUpdateInstaller> { AppUpdateInstallerImpl(context = androidContext()) }
        factory { CheckForUpdateUseCase(get()) }
        factory { DownloadAppUpdateUseCase(get()) }
        factory { ClearDownloadedUpdateUseCase(get()) }
    }
