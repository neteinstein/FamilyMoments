package org.neteinstein.family.data.di

import org.koin.dsl.module
import org.neteinstein.family.data.repository.QuestionRepositoryImpl
import org.neteinstein.family.domain.repository.QuestionRepository
import org.neteinstein.family.domain.usecase.GetQuestionsUseCase
import org.neteinstein.family.domain.usecase.GetRandomQuestionUseCase

val dataModule = module {
    single<QuestionRepository> { QuestionRepositoryImpl() }
    factory { GetRandomQuestionUseCase(get()) }
    factory { GetQuestionsUseCase(get()) }
}
