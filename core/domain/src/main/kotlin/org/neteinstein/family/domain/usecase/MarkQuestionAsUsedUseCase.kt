package org.neteinstein.family.domain.usecase

import org.neteinstein.family.domain.repository.QuestionRepository

class MarkQuestionAsUsedUseCase(
    private val questionRepository: QuestionRepository
) {
    suspend operator fun invoke(questionId: Int) =
        questionRepository.markQuestionAsUsed(questionId)
}
