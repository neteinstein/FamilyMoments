package org.neteinstein.family.data.repository

import kotlinx.coroutines.test.runTest
import org.neteinstein.family.data.local.QuestionLocalDataSource
import org.neteinstein.family.domain.model.Question
import kotlin.test.Test
import kotlin.test.assertEquals

private class FakeQuestionLocalDataSource : QuestionLocalDataSource {
    var hiddenIds: Set<Int> = emptySet()
    var lastMarkedHiddenId: Int? = null
    var resetAllHiddenCallCount = 0

    override suspend fun getSeedVersion(): Int? = null

    override suspend fun replaceAllCards(
        questions: List<Question>,
        hiddenIds: Set<Int>,
        seedVersion: Int,
    ) = Unit

    override suspend fun getQuestionsForLanguage(languageCode: String): List<Question> = emptyList()

    override suspend fun getHiddenIds(): Set<Int> = hiddenIds

    override suspend fun markHidden(questionId: Int) {
        lastMarkedHiddenId = questionId
        hiddenIds = hiddenIds + questionId
    }

    override suspend fun resetAllHidden() {
        resetAllHiddenCallCount++
        hiddenIds = emptySet()
    }
}

class UsedQuestionsRepositoryImplTest {
    private val localDataSource = FakeQuestionLocalDataSource()
    private val repository = UsedQuestionsRepositoryImpl(localDataSource)

    @Test
    fun `getUsedQuestionIds returns empty set when nothing is hidden`() =
        runTest {
            assertEquals(emptySet(), repository.getUsedQuestionIds())
        }

    @Test
    fun `getUsedQuestionIds returns the hidden ids from the database`() =
        runTest {
            localDataSource.hiddenIds = setOf(1, 2, 3)

            assertEquals(setOf(1, 2, 3), repository.getUsedQuestionIds())
        }

    @Test
    fun `markAsUsed hides the card in the database`() =
        runTest {
            repository.markAsUsed(2)

            assertEquals(2, localDataSource.lastMarkedHiddenId)
        }

    @Test
    fun `resetUsedQuestions clears every hidden card`() =
        runTest {
            localDataSource.hiddenIds = setOf(1, 2, 3)

            repository.resetUsedQuestions()

            assertEquals(1, localDataSource.resetAllHiddenCallCount)
            assertEquals(emptySet(), repository.getUsedQuestionIds())
        }
}
