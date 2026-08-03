package org.neteinstein.family.domain.usecase

import org.neteinstein.family.domain.repository.QuestionRepository

class MarkAllQuestionsAsUnusedUseCase(
    private val questionRepository: QuestionRepository
) {
    suspend operator fun invoke() =
        questionRepository.markAllQuestionsAsUnused()
}
