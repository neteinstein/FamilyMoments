package org.neteinstein.family.data.repository

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.neteinstein.family.data.local.QuestionLocalDataSource
import org.neteinstein.family.data.source.QuestionSeedData
import org.neteinstein.family.domain.model.Question
import org.neteinstein.family.domain.repository.QuestionRepository

class QuestionRepositoryImpl(
    private val localDataSource: QuestionLocalDataSource,
) : QuestionRepository {
    private val seedMutex = Mutex()
    private var isSeeded = false

    override suspend fun getQuestions(languageCode: String): List<Question> {
        ensureSeeded()
        return localDataSource.getQuestionsForLanguage(languageCode)
    }

    override suspend fun getRandomQuestion(languageCode: String): Question? = getQuestions(languageCode).randomOrNull()

    /**
     * Fully replaces the stored cards with [QuestionSeedData] whenever the stored seed version
     * doesn't match [QuestionSeedData.VERSION] - once per process lifetime otherwise. A full
     * replace (rather than an additive, conflict-ignoring insert) is the only way a question
     * removed from [QuestionSeedData] actually disappears from a device seeded before it was
     * deleted; an additive insert can only ever add rows. Cards already marked hidden are re-marked
     * hidden after the replace (matched by id) so a version bump doesn't silently un-hide every
     * card a user has already swiped away.
     */
    private suspend fun ensureSeeded() {
        if (isSeeded) return
        seedMutex.withLock {
            if (isSeeded) return
            if (localDataSource.getSeedVersion() != QuestionSeedData.VERSION) {
                val hiddenIds = localDataSource.getHiddenIds()
                localDataSource.replaceAllCards(QuestionSeedData.all, hiddenIds, QuestionSeedData.VERSION)
            }
            isSeeded = true
        }
    }
}
