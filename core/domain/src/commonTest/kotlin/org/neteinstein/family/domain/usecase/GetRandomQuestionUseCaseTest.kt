package org.neteinstein.family.domain.usecase

import kotlinx.coroutines.test.runTest
import org.neteinstein.family.domain.model.Question
import org.neteinstein.family.domain.repository.QuestionRepository
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

private class FakeQuestionRepository : QuestionRepository {
    var randomQuestion: Question? = null
    var lastRequestedLanguageCode: String? = null

    override suspend fun getQuestions(languageCode: String): List<Question> = emptyList()

    override suspend fun getRandomQuestion(languageCode: String): Question? {
        lastRequestedLanguageCode = languageCode
        return randomQuestion
    }
}

class GetRandomQuestionUseCaseTest {
    private val repository = FakeQuestionRepository()
    private val useCase = GetRandomQuestionUseCase(repository)

    @Test
    fun `invoke returns question from repository`() =
        runTest {
            val question = Question(id = 1, text = "Test question?", languageCode = "en")
            repository.randomQuestion = question

            val result = useCase("en")

            assertEquals(question, result)
        }

    @Test
    fun `invoke returns null when no questions available`() =
        runTest {
            repository.randomQuestion = null

            val result = useCase("en")

            assertNull(result)
        }

    @Test
    fun `invoke passes language code to repository`() =
        runTest {
            repository.randomQuestion = null

            useCase("pt")

            assertEquals("pt", repository.lastRequestedLanguageCode)
        }
}
