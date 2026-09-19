package org.neteinstein.family.data.repository

import org.neteinstein.family.data.local.QuestionLocalDataSource
import org.neteinstein.family.domain.repository.UsedQuestionsRepository

class UsedQuestionsRepositoryImpl(
    private val localDataSource: QuestionLocalDataSource,
) : UsedQuestionsRepository {
    override suspend fun getUsedQuestionIds(): Set<Int> = localDataSource.getHiddenIds()

    override suspend fun markAsUsed(questionId: Int) {
        localDataSource.markHidden(questionId)
    }

    override suspend fun resetUsedQuestions() {
        localDataSource.resetAllHidden()
    }
}
