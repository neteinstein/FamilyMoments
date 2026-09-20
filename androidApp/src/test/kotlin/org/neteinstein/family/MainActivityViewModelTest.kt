package org.neteinstein.family

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.neteinstein.family.domain.model.ThemeMode
import org.neteinstein.family.domain.usecase.GetThemeModeUseCase

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
    private val getThemeModeUseCase: GetThemeModeUseCase = mockk()

    private lateinit var viewModel: MainActivityViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        every { getThemeModeUseCase() } returns themeModeFlow
        viewModel = MainActivityViewModel(getThemeModeUseCase)
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
}
