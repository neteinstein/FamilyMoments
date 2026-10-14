package org.neteinstein.family.data.local

import kotlinx.coroutines.await
import org.neteinstein.family.domain.model.Question
import org.neteinstein.family.domain.model.QuestionCategory
import kotlin.js.ExperimentalWasmJsInterop
import kotlin.js.JsAny
import kotlin.js.JsNumber
import kotlin.js.JsString

private const val FIELD_SEPARATOR = '\u001f'
private const val RECORD_SEPARATOR = '\u001e'

private fun QuestionCategory.toStorageKey(): String =
    when (this) {
        QuestionCategory.IceBreakers -> "ice_breakers"
        QuestionCategory.Memories -> "memories"
        QuestionCategory.Values -> "values"
        QuestionCategory.FutureDreams -> "future_dreams"
        QuestionCategory.DailyLife -> "daily_life"
    }

private fun categoryFromStorageKey(key: String): QuestionCategory =
    when (key) {
        "ice_breakers" -> QuestionCategory.IceBreakers
        "memories" -> QuestionCategory.Memories
        "values" -> QuestionCategory.Values
        "future_dreams" -> QuestionCategory.FutureDreams
        "daily_life" -> QuestionCategory.DailyLife
        else -> QuestionCategory.IceBreakers
    }

/** Strips the control characters [toRow]/[rowToQuestion] use as delimiters - seed question text never contains them. */
private fun String.sanitizedForRow(): String = replace(RECORD_SEPARATOR.toString(), "").replace(FIELD_SEPARATOR.toString(), "")

private fun Question.toRow(): String =
    listOf(id.toString(), text.sanitizedForRow(), languageCode, category.toStorageKey())
        .joinToString(FIELD_SEPARATOR.toString())

private fun rowToQuestion(row: String): Question {
    val fields = row.split(FIELD_SEPARATOR)
    return Question(
        id = fields[0].toInt(),
        text = fields[1],
        languageCode = fields[2],
        category = categoryFromStorageKey(fields[3]),
    )
}

/**
 * IndexedDB-backed [QuestionLocalDataSource] - persists across page reloads, unlike the in-memory
 * version this replaces. See [IndexedDbInterop][jsInstallIdbHelper]'s file-level doc comment for
 * the Kotlin/Wasm-JS interop and row encoding this builds on.
 */
private class IndexedDbQuestionLocalDataSource : QuestionLocalDataSource {
    @OptIn(ExperimentalWasmJsInterop::class)
    override suspend fun getSeedVersion(): Int? {
        jsInstallIdbHelper()
        return idbGetSeedVersion().await<JsNumber?>()?.toInt()
    }

    @OptIn(ExperimentalWasmJsInterop::class)
    override suspend fun replaceAllCards(
        questions: List<Question>,
        hiddenIds: Set<Int>,
        seedVersion: Int,
    ) {
        jsInstallIdbHelper()
        val cardRows = questions.joinToString(RECORD_SEPARATOR.toString()) { it.toRow() }
        val hiddenIdsCsv = hiddenIds.joinToString(",")
        idbReplaceAllCards(cardRows, hiddenIdsCsv, seedVersion).await<JsAny?>()
    }

    @OptIn(ExperimentalWasmJsInterop::class)
    override suspend fun getQuestionsForLanguage(languageCode: String): List<Question> {
        jsInstallIdbHelper()
        val payload = idbGetQuestionsForLanguage(languageCode).await<JsString?>()?.toString().orEmpty()
        if (payload.isEmpty()) return emptyList()
        return payload.split(RECORD_SEPARATOR).map(::rowToQuestion)
    }

    @OptIn(ExperimentalWasmJsInterop::class)
    override suspend fun getHiddenIds(): Set<Int> {
        jsInstallIdbHelper()
        val csv = idbGetHiddenIds().await<JsString?>()?.toString().orEmpty()
        if (csv.isEmpty()) return emptySet()
        return csv.split(",").map { it.toInt() }.toSet()
    }

    @OptIn(ExperimentalWasmJsInterop::class)
    override suspend fun markHidden(questionId: Int) {
        jsInstallIdbHelper()
        idbMarkHidden(questionId).await<JsAny?>()
    }

    @OptIn(ExperimentalWasmJsInterop::class)
    override suspend fun resetAllHidden() {
        jsInstallIdbHelper()
        idbResetAllHidden().await<JsAny?>()
    }
}

actual fun createQuestionLocalDataSource(): QuestionLocalDataSource = IndexedDbQuestionLocalDataSource()
