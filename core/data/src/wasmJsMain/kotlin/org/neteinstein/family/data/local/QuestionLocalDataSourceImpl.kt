package org.neteinstein.family.data.local

import org.neteinstein.family.domain.model.Question

/**
 * In-memory only for now - Room doesn't support wasmJs at all yet. Data does not survive a page
 * reload until a browser-storage-backed implementation replaces this (see
 * [QuestionLocalDataSource]'s doc comment).
 */
private class InMemoryQuestionLocalDataSource : QuestionLocalDataSource {
    private var seedVersion: Int? = null
    private var questions: List<Question> = emptyList()
    private var hiddenIds: Set<Int> = emptySet()

    override suspend fun getSeedVersion(): Int? = seedVersion

    override suspend fun replaceAllCards(
        questions: List<Question>,
        hiddenIds: Set<Int>,
        seedVersion: Int,
    ) {
        this.questions = questions
        this.hiddenIds = hiddenIds
        this.seedVersion = seedVersion
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

actual fun createQuestionLocalDataSource(): QuestionLocalDataSource = InMemoryQuestionLocalDataSource()
