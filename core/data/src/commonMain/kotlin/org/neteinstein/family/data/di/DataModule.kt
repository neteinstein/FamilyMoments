package org.neteinstein.family.data.di

import org.koin.core.module.Module
import org.koin.dsl.module
import org.neteinstein.family.data.local.KeyValueStore
import org.neteinstein.family.data.local.QuestionLocalDataSource
import org.neteinstein.family.data.local.createQuestionLocalDataSource
import org.neteinstein.family.data.local.platformKeyValueStore
import org.neteinstein.family.data.locale.LocaleProviderImpl
import org.neteinstein.family.data.preferences.ThemePreferenceRepositoryImpl
import org.neteinstein.family.data.repository.QuestionRepositoryImpl
import org.neteinstein.family.data.repository.UsedQuestionsRepositoryImpl
import org.neteinstein.family.domain.repository.LocaleProvider
import org.neteinstein.family.domain.repository.QuestionRepository
import org.neteinstein.family.domain.repository.ThemePreferenceRepository
import org.neteinstein.family.domain.repository.UsedQuestionsRepository
import org.neteinstein.family.domain.usecase.GetQuestionsUseCase
import org.neteinstein.family.domain.usecase.GetRandomQuestionUseCase
import org.neteinstein.family.domain.usecase.GetThemeModeUseCase
import org.neteinstein.family.domain.usecase.GetUsedQuestionIdsUseCase
import org.neteinstein.family.domain.usecase.MarkQuestionUsedUseCase
import org.neteinstein.family.domain.usecase.ResetUsedQuestionsUseCase
import org.neteinstein.family.domain.usecase.SetThemeModeUseCase

/**
 * The GitHub self-update feature (Android-only - APK sideloading has no iOS/Web equivalent) is
 * registered by [platformUpdateModule] instead of living here - see each target's actual.
 */
val dataModule =
    module {
        single<QuestionLocalDataSource> { createQuestionLocalDataSource() }
        single<KeyValueStore> { platformKeyValueStore() }
        single<QuestionRepository> { QuestionRepositoryImpl(get()) }
        single<LocaleProvider> { LocaleProviderImpl() }
        single<UsedQuestionsRepository> { UsedQuestionsRepositoryImpl(get()) }
        single<ThemePreferenceRepository> { ThemePreferenceRepositoryImpl(get()) }
        factory { GetThemeModeUseCase(get()) }
        factory { SetThemeModeUseCase(get()) }
        factory { GetRandomQuestionUseCase(get()) }
        factory { GetQuestionsUseCase(get()) }
        factory { GetUsedQuestionIdsUseCase(get()) }
        factory { MarkQuestionUsedUseCase(get()) }
        factory { ResetUsedQuestionsUseCase(get()) }

        includes(platformUpdateModule)
    }

expect val platformUpdateModule: Module
