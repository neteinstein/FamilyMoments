package org.neteinstein.family.data.local

import androidx.room.Room
import org.neteinstein.family.domain.model.Question

/** Room-backed [QuestionLocalDataSource] - the same Room setup this app used before the KMP migration. */
class RoomQuestionLocalDataSource(
    private val cardDao: CardDao,
    private val seedMetadataDao: SeedMetadataDao,
) : QuestionLocalDataSource {
    override suspend fun getSeedVersion(): Int? = seedMetadataDao.getVersion()

    override suspend fun replaceAllCards(
        questions: List<Question>,
        hiddenIds: Set<Int>,
        seedVersion: Int,
    ) {
        cardDao.deleteAll()
        cardDao.insertAll(questions.map { it.toEntity().copy(isHidden = it.id in hiddenIds) })
        seedMetadataDao.setVersion(SeedMetadataEntity(version = seedVersion))
    }

    override suspend fun getQuestionsForLanguage(languageCode: String): List<Question> =
        cardDao.getCardsForLanguage(languageCode).map { it.toDomain() }

    override suspend fun getHiddenIds(): Set<Int> = cardDao.getHiddenIds().toSet()

    override suspend fun markHidden(questionId: Int) {
        cardDao.markHidden(questionId)
    }

    override suspend fun resetAllHidden() {
        cardDao.resetAllHidden()
    }
}

actual fun createQuestionLocalDataSource(): QuestionLocalDataSource {
    val database =
        Room
            .databaseBuilder(AndroidAppContext.instance, FamilyMomentsDatabase::class.java, "family_moments.db")
            .addMigrations(FamilyMomentsDatabase.MIGRATION_1_2)
            .build()
    return RoomQuestionLocalDataSource(database.cardDao(), database.seedMetadataDao())
}
