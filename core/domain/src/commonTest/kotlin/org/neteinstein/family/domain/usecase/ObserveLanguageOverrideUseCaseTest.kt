package org.neteinstein.family.domain.usecase

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.neteinstein.family.domain.model.AppLanguage
import org.neteinstein.family.domain.repository.LanguagePreferenceRepository
import kotlin.test.Test
import kotlin.test.assertEquals

private class StreamingLanguagePreferenceRepository : LanguagePreferenceRepository {
    private val overrideState = MutableStateFlow<AppLanguage?>(null)

    override val languageOverride: Flow<AppLanguage?> = overrideState

    override fun getLanguageOverride(): AppLanguage? = overrideState.value

    override suspend fun setLanguageOverride(language: AppLanguage?) {
        overrideState.value = language
    }
}

class ObserveLanguageOverrideUseCaseTest {
    private val repository = StreamingLanguagePreferenceRepository()
    private val useCase = ObserveLanguageOverrideUseCase(repository)

    @Test
    fun `invoke starts with no override`() =
        runTest {
            assertEquals(null, useCase().first())
        }

    @Test
    fun `invoke emits each language the user picks`() =
        runTest {
            val collected = mutableListOf<AppLanguage?>()
            val job = launch { useCase().toList(collected) }
            runCurrent()

            repository.setLanguageOverride(AppLanguage.GERMAN)
            runCurrent()
            repository.setLanguageOverride(null)
            runCurrent()
            job.cancel()

            assertEquals(listOf(null, AppLanguage.GERMAN, null), collected)
        }
}
