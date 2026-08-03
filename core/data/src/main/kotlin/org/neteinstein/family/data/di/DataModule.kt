package org.neteinstein.family.data.di

import android.content.Context
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module
import org.neteinstein.family.data.repository.QuestionRepositoryImpl
import org.neteinstein.family.domain.repository.QuestionRepository
import org.neteinstein.family.domain.usecase.GetQuestionsUseCase
import org.neteinstein.family.domain.usecase.GetRandomQuestionUseCase
import org.neteinstein.family.domain.usecase.MarkAllQuestionsAsUnusedUseCase
import org.neteinstein.family.domain.usecase.MarkQuestionAsUsedUseCase

private const val PREFS_NAME = "family_moments_prefs"

val dataModule = module {
    single {
        androidContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }
    single<QuestionRepository> { QuestionRepositoryImpl(get()) }
    factory { GetRandomQuestionUseCase(get()) }
    factory { GetQuestionsUseCase(get()) }
    factory { MarkQuestionAsUsedUseCase(get()) }
    factory { MarkAllQuestionsAsUnusedUseCase(get()) }
}
