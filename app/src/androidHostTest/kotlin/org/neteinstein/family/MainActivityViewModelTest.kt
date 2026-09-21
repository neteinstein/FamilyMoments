package org.neteinstein.family

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.neteinstein.family.domain.model.AppLanguage
import org.neteinstein.family.domain.model.ThemeMode
import org.neteinstein.family.domain.usecase.GetLanguageOverrideUseCase
import org.neteinstein.family.domain.usecase.GetThemeModeUseCase
import org.neteinstein.family.domain.usecase.ObserveLanguageOverrideUseCase

@OptIn(ExperimentalCoroutinesApi::class)
class MainActivityViewModelTest {
    private val testDispatcher = StandardTestDispatcher()
    private val themeModeFlow = MutableStateFlow(ThemeMode.SYSTEM)
    private val languageOverrideFlow = MutableStateFlow<AppLanguage?>(AppLanguage.PORTUGUESE)
    private val getThemeModeUseCase: GetThemeModeUseCase = mockk()
    private val getLanguageOverrideUseCase: GetLanguageOverrideUseCase = mockk()
    private val observeLanguageOverrideUseCase: ObserveLanguageOverrideUseCase = mockk()

    private lateinit var viewModel: MainActivityViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        every { getThemeModeUseCase() } returns themeModeFlow
        every { getLanguageOverrideUseCase() } returns languageOverrideFlow.value
        every { observeLanguageOverrideUseCase() } returns languageOverrideFlow
        viewModel = MainActivityViewModel(getThemeModeUseCase, getLanguageOverrideUseCase, observeLanguageOverrideUseCase)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `themeMode starts at SYSTEM before the persisted value loads`() {
        assertEquals(ThemeMode.SYSTEM, viewModel.themeMode.value)
    }

    @Test
    fun `themeMode is backed by GetThemeModeUseCase`() {
        verify { getThemeModeUseCase() }
    }

    @Test
    fun `languageOverride starts at the persisted override so the first frame is localized`() {
        assertEquals(AppLanguage.PORTUGUESE, viewModel.languageOverride.value)
    }

    @Test
    fun `languageOverride follows later changes from the Settings picker`() =
        runTest(testDispatcher) {
            val collected = mutableListOf<AppLanguage?>()
            val job = launch { viewModel.languageOverride.toList(collected) }
            runCurrent()

            languageOverrideFlow.value = AppLanguage.GERMAN
            runCurrent()
            job.cancel()

            assertEquals(listOf(AppLanguage.PORTUGUESE, AppLanguage.GERMAN), collected)
        }
}
