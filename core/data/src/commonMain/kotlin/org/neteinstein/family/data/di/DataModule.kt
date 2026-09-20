package org.neteinstein.family.data.di

import org.koin.core.module.Module
import org.koin.dsl.module
import org.neteinstein.family.data.local.KeyValueStore
import org.neteinstein.family.data.local.QuestionLocalDataSource
import org.neteinstein.family.data.local.createQuestionLocalDataSource
import org.neteinstein.family.data.local.platformKeyValueStore
import org.neteinstein.family.data.locale.LocaleProviderImpl
import org.neteinstein.family.data.preferences.LanguagePreferenceRepositoryImpl
import org.neteinstein.family.data.preferences.ThemePreferenceRepositoryImpl
import org.neteinstein.family.data.repository.QuestionRepositoryImpl
import org.neteinstein.family.data.repository.UsedQuestionsRepositoryImpl
import org.neteinstein.family.domain.repository.LanguagePreferenceRepository
import org.neteinstein.family.domain.repository.LocaleProvider
import org.neteinstein.family.domain.repository.QuestionRepository
import org.neteinstein.family.domain.repository.ThemePreferenceRepository
import org.neteinstein.family.domain.repository.UsedQuestionsRepository
import org.neteinstein.family.domain.usecase.CheckForUpdateUseCase
import org.neteinstein.family.domain.usecase.ClearDownloadedUpdateUseCase
import org.neteinstein.family.domain.usecase.DownloadAppUpdateUseCase
import org.neteinstein.family.domain.usecase.GetContentLanguageUseCase
import org.neteinstein.family.domain.usecase.GetLanguageOverrideUseCase
import org.neteinstein.family.domain.usecase.GetQuestionsUseCase
import org.neteinstein.family.domain.usecase.GetRandomQuestionUseCase
import org.neteinstein.family.domain.usecase.GetThemeModeUseCase
import org.neteinstein.family.domain.usecase.GetUsedQuestionIdsUseCase
import org.neteinstein.family.domain.usecase.MarkQuestionUsedUseCase
import org.neteinstein.family.domain.usecase.ResetUsedQuestionsUseCase
import org.neteinstein.family.domain.usecase.SetLanguageOverrideUseCase
import org.neteinstein.family.domain.usecase.SetThemeModeUseCase

/**
 * The concrete GitHub self-update implementation (Android-only - APK sideloading has no iOS/Web
 * equivalent) is registered by [platformUpdateModule] instead of living here - see each target's
 * actual. The use cases wrapping it are common code, so they're registered below like any other.
 */
val dataModule =
    module {
        single<QuestionLocalDataSource> { createQuestionLocalDataSource() }
        single<KeyValueStore> { platformKeyValueStore() }
        single<QuestionRepository> { QuestionRepositoryImpl(get()) }
        single<LocaleProvider> { LocaleProviderImpl() }
        single<UsedQuestionsRepository> { UsedQuestionsRepositoryImpl(get()) }
        single<ThemePreferenceRepository> { ThemePreferenceRepositoryImpl(get()) }
        single<LanguagePreferenceRepository> { LanguagePreferenceRepositoryImpl(get()) }
        factory { GetThemeModeUseCase(get()) }
        factory { SetThemeModeUseCase(get()) }
        factory { GetContentLanguageUseCase(get(), get()) }
        factory { GetLanguageOverrideUseCase(get()) }
        factory { SetLanguageOverrideUseCase(get()) }
        factory { GetRandomQuestionUseCase(get()) }
        factory { GetQuestionsUseCase(get()) }
        factory { GetUsedQuestionIdsUseCase(get()) }
        factory { MarkQuestionUsedUseCase(get()) }
        factory { ResetUsedQuestionsUseCase(get()) }
        factory { CheckForUpdateUseCase(get()) }
        factory { DownloadAppUpdateUseCase(get()) }
        factory { ClearDownloadedUpdateUseCase(get()) }

        includes(platformUpdateModule)
    }

expect val platformUpdateModule: Module
