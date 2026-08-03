package org.neteinstein.family.data.repository

import android.content.SharedPreferences
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class QuestionRepositoryImplTest {

    private val sharedPreferences: SharedPreferences = mockk(relaxed = true)
    private lateinit var repository: QuestionRepositoryImpl

    @Before
    fun setUp() {
        every { sharedPreferences.getStringSet(any(), any()) } returns emptySet()
        repository = QuestionRepositoryImpl(sharedPreferences)
    }

    @Test
    fun `getQuestions returns english questions for en locale`() = runTest {
        val questions = repository.getQuestions("en")

        assertTrue(questions.isNotEmpty())
        assertTrue(questions.all { it.languageCode == "en" })
    }

    @Test
    fun `getQuestions returns portuguese questions for pt locale`() = runTest {
        val questions = repository.getQuestions("pt")

        assertTrue(questions.isNotEmpty())
        assertTrue(questions.all { it.languageCode == "pt" })
    }

    @Test
    fun `getQuestions defaults to english for unknown locale`() = runTest {
        val questions = repository.getQuestions("de")

        assertTrue(questions.isNotEmpty())
        assertTrue(questions.all { it.languageCode == "en" })
    }

    @Test
    fun `getRandomQuestion returns a question`() = runTest {
        val question = repository.getRandomQuestion("en")

        assertNotNull(question)
    }

    @Test
    fun `getRandomQuestion returns portuguese question for pt locale`() = runTest {
        val question = repository.getRandomQuestion("pt")

        assertNotNull(question)
        assertTrue(question?.languageCode == "pt")
    }
}
