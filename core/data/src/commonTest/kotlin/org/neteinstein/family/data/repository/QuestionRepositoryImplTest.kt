package org.neteinstein.family.data.repository

import kotlinx.coroutines.test.runTest
import org.neteinstein.family.data.local.QuestionLocalDataSource
import org.neteinstein.family.data.source.QuestionSeedData
import org.neteinstein.family.domain.model.Question
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

private class FakeQuestionLocalDataSource : QuestionLocalDataSource {
    var seedVersion: Int? = null
    var hiddenIds: Set<Int> = emptySet()
    var deleteAndInsertCallCount = 0
    var lastReplacedQuestions: List<Question> = emptyList()
    var lastReplacedHiddenIds: Set<Int> = emptySet()
    var lastSetVersion: Int? = null

    private var questions: List<Question> = emptyList()

    override suspend fun getSeedVersion(): Int? = seedVersion

    override suspend fun replaceAllCards(
        questions: List<Question>,
        hiddenIds: Set<Int>,
        seedVersion: Int,
    ) {
        deleteAndInsertCallCount++
        this.questions = questions
        this.hiddenIds = hiddenIds
        this.seedVersion = seedVersion
        lastReplacedQuestions = questions
        lastReplacedHiddenIds = hiddenIds
        lastSetVersion = seedVersion
    }

    override suspend fun getQuestionsForLanguage(languageCode: String): List<Question> =
        questions.filter { it.languageCode == languageCode }

    override suspend fun getHiddenIds(): Set<Int> = hiddenIds

    override suspend fun markHidden(questionId: Int) {
        hiddenIds = hiddenIds + questionId
    }

    override suspend fun resetAllHidden() {
        hiddenIds = emptySet()
    }
}

class QuestionRepositoryImplTest {
    private val localDataSource = FakeQuestionLocalDataSource()
    private val repository = QuestionRepositoryImpl(localDataSource)

    @Test
    fun `getQuestions returns english questions for en locale`() =
        runTest {
            val questions = repository.getQuestions("en")

            assertTrue(questions.isNotEmpty())
            assertTrue(questions.all { it.languageCode == "en" })
        }

    @Test
    fun `getQuestions returns portuguese questions for pt locale`() =
        runTest {
            val questions = repository.getQuestions("pt")

            assertTrue(questions.isNotEmpty())
            assertTrue(questions.all { it.languageCode == "pt" })
        }

    @Test
    fun `getRandomQuestion returns a question`() =
        runTest {
            val question = repository.getRandomQuestion("en")

            assertNotNull(question)
        }

    @Test
    fun `getRandomQuestion returns portuguese question for pt locale`() =
        runTest {
            val question = repository.getRandomQuestion("pt")

            assertNotNull(question)
            assertTrue(question?.languageCode == "pt")
        }

    @Test
    fun `seeds the database from seed data only once per process lifetime`() =
        runTest {
            repository.getQuestions("en")
            repository.getQuestions("en")

            assertEquals(1, localDataSource.deleteAndInsertCallCount)
        }

    @Test
    fun `replaces the database when the stored seed version differs from the current version`() =
        runTest {
            localDataSource.seedVersion = QuestionSeedData.VERSION + 1

            repository.getQuestions("en")

            assertEquals(1, localDataSource.deleteAndInsertCallCount)
            assertEquals(QuestionSeedData.VERSION, localDataSource.lastSetVersion)
        }

    @Test
    fun `does not replace the database when the stored version already matches`() =
        runTest {
            localDataSource.seedVersion = QuestionSeedData.VERSION

            repository.getQuestions("en")

            assertEquals(0, localDataSource.deleteAndInsertCallCount)
        }

    @Test
    fun `preserves already-hidden card ids across a version-triggered replace`() =
        runTest {
            val hiddenId = QuestionSeedData.all.first().id
            localDataSource.hiddenIds = setOf(hiddenId)

            repository.getQuestions("en")

            assertEquals(setOf(hiddenId), localDataSource.lastReplacedHiddenIds)
        }

    @Test
    fun `replace reinserts every seed card`() =
        runTest {
            repository.getQuestions("en")

            assertEquals(QuestionSeedData.all.size, localDataSource.lastReplacedQuestions.size)
        }
}
