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
import org.neteinstein.family.domain.analytics.AnalyticsTracker
import org.neteinstein.family.domain.model.AppLanguage
import org.neteinstein.family.domain.model.ThemeMode
import org.neteinstein.family.domain.usecase.GetContentLanguageUseCase
import org.neteinstein.family.domain.usecase.GetLanguageOverrideUseCase
import org.neteinstein.family.domain.usecase.GetThemeModeUseCase
import org.neteinstein.family.domain.usecase.ObserveLanguageOverrideUseCase

// MainActivityViewModel itself now lives in `app`'s commonMain (Phase 6), which has its own real
// copy of this test under androidHostTest. This copy stays here only to keep androidApp's own
// createGithubDebugUnitTestCoverageReport task satisfied (AGP hard-fails it if a module enables
// coverage but has zero unit tests) - it's the one reason androidApp's build.gradle.kts still
// declares a (test-only) dependency on core:domain at all. Real per-module KMP coverage,
// replacing this stopgap, is a Phase 8 follow-up.
@OptIn(ExperimentalCoroutinesApi::class)
class MainActivityViewModelTest {
    private val testDispatcher = StandardTestDispatcher()
    private val themeModeFlow = MutableStateFlow(ThemeMode.SYSTEM)
    private val languageOverrideFlow = MutableStateFlow<AppLanguage?>(AppLanguage.PORTUGUESE)
    private val getThemeModeUseCase: GetThemeModeUseCase = mockk()
    private val getLanguageOverrideUseCase: GetLanguageOverrideUseCase = mockk()
    private val observeLanguageOverrideUseCase: ObserveLanguageOverrideUseCase = mockk()
    private val getContentLanguageUseCase: GetContentLanguageUseCase = mockk()

    // Relaxed: this ViewModel sets analytics user properties on construction and on every
    // preference change, none of which these theme/language assertions are about.
    private val analyticsTracker: AnalyticsTracker = mockk(relaxed = true)

    private lateinit var viewModel: MainActivityViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        every { getThemeModeUseCase() } returns themeModeFlow
        every { getLanguageOverrideUseCase() } returns languageOverrideFlow.value
        every { observeLanguageOverrideUseCase() } returns languageOverrideFlow
        every { getContentLanguageUseCase() } returns "en"
        viewModel =
            MainActivityViewModel(
                getThemeModeUseCase,
                getLanguageOverrideUseCase,
                observeLanguageOverrideUseCase,
                getContentLanguageUseCase,
                analyticsTracker,
                platform = "android",
                distribution = "github",
            )
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
