package org.neteinstein.family.data.repository

import android.content.SharedPreferences
import org.neteinstein.family.data.source.QuestionDataSource
import org.neteinstein.family.domain.model.Question
import org.neteinstein.family.domain.repository.QuestionRepository

private const val PREFS_KEY_USED_IDS = "used_question_ids"

class QuestionRepositoryImpl(
    private val sharedPreferences: SharedPreferences,
    private val dataSource: QuestionDataSource = QuestionDataSource
) : QuestionRepository {

    override suspend fun getQuestions(languageCode: String): List<Question> {
        val usedIds = getUsedIds()
        return dataSource.getQuestions(languageCode).filter { it.id !in usedIds }
    }

    override suspend fun getRandomQuestion(languageCode: String): Question? {
        val usedIds = getUsedIds()
        return dataSource.getQuestions(languageCode)
            .filter { it.id !in usedIds }
            .randomOrNull()
    }

    override suspend fun markQuestionAsUsed(questionId: Int) {
        val usedIds = getUsedIds().toMutableSet()
        usedIds.add(questionId.toString())
        sharedPreferences.edit().putStringSet(PREFS_KEY_USED_IDS, usedIds).apply()
    }

    override suspend fun markAllQuestionsAsUnused() {
        sharedPreferences.edit().remove(PREFS_KEY_USED_IDS).apply()
    }

    private fun getUsedIds(): Set<Int> =
        sharedPreferences.getStringSet(PREFS_KEY_USED_IDS, emptySet())
            ?.mapNotNull { it.toIntOrNull() }
            ?.toSet()
            ?: emptySet()
}
