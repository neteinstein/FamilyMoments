package org.neteinstein.family.data.local

import org.neteinstein.family.domain.model.Question

/**
 * Local storage for seeded question cards and their "used" (hidden) state, behind
 * [org.neteinstein.family.data.repository.QuestionRepositoryImpl] /
 * [org.neteinstein.family.data.repository.UsedQuestionsRepositoryImpl].
 *
 * Room-backed on Android (unchanged from this app's pre-KMP implementation - see the androidMain
 * actual). Room's KMP support needs a per-target SQLite driver we haven't set up for iOS yet, so
 * iOS and wasmJs get a simple in-memory actual for now - same interim choice, and for the same
 * reason, as loopgain's own still-unimplemented `SessionHistoryRepository`. A later pass can swap
 * in a real iOS driver (Room supports it) and browser storage for wasmJs without this interface,
 * or any of its callers, needing to change.
 */
interface QuestionLocalDataSource {
    suspend fun getSeedVersion(): Int?

    /** Fully replaces the stored cards with [questions], re-marking [hiddenIds] as hidden, and records [seedVersion]. */
    suspend fun replaceAllCards(
        questions: List<Question>,
        hiddenIds: Set<Int>,
        seedVersion: Int,
    )

    suspend fun getQuestionsForLanguage(languageCode: String): List<Question>

    suspend fun getHiddenIds(): Set<Int>

    suspend fun markHidden(questionId: Int)

    suspend fun resetAllHidden()
}

expect fun createQuestionLocalDataSource(): QuestionLocalDataSource
