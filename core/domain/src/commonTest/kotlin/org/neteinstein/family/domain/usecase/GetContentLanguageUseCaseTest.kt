package org.neteinstein.family.domain.usecase

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import org.neteinstein.family.domain.model.AppLanguage
import org.neteinstein.family.domain.repository.LanguagePreferenceRepository
import org.neteinstein.family.domain.repository.LocaleProvider
import kotlin.test.Test
import kotlin.test.assertEquals

private class FakeLocaleProvider(
    var languageCode: String = "en",
) : LocaleProvider {
    override fun currentLanguageCode(): String = languageCode
}

private class FakeLanguagePreferenceRepository(
    var override: AppLanguage? = null,
) : LanguagePreferenceRepository {
    private val overrideState = MutableStateFlow(override)

    override val languageOverride: Flow<AppLanguage?> = overrideState

    override fun getLanguageOverride(): AppLanguage? = override

    override suspend fun setLanguageOverride(language: AppLanguage?) {
        override = language
        overrideState.value = language
    }
}

class GetContentLanguageUseCaseTest {
    private val localeProvider = FakeLocaleProvider()
    private val languagePreferenceRepository = FakeLanguagePreferenceRepository()
    private val useCase = GetContentLanguageUseCase(localeProvider, languagePreferenceRepository)

    @Test
    fun `invoke returns the user's override when one is set`() {
        localeProvider.languageCode = "en"
        languagePreferenceRepository.override = AppLanguage.PORTUGUESE

        assertEquals("pt", useCase())
    }

    @Test
    fun `invoke falls back to the OS locale when no override is set`() {
        localeProvider.languageCode = "es"
        languagePreferenceRepository.override = null

        assertEquals("es", useCase())
    }

    @Test
    fun `invoke falls back to English when the OS locale isn't a supported language`() {
        localeProvider.languageCode = "ja"
        languagePreferenceRepository.override = null

        assertEquals("en", useCase())
    }
}
