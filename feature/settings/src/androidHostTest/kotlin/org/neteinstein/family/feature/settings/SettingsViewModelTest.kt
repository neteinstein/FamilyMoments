package org.neteinstein.family.feature.settings

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.neteinstein.family.domain.analytics.ANALYTICS_VALUE_AUTOMATIC
import org.neteinstein.family.domain.analytics.AnalyticsEvents
import org.neteinstein.family.domain.analytics.AnalyticsParams
import org.neteinstein.family.domain.analytics.AnalyticsTracker
import org.neteinstein.family.domain.model.AppLanguage
import org.neteinstein.family.domain.model.AppUpdate
import org.neteinstein.family.domain.model.Question
import org.neteinstein.family.domain.model.QuestionCategory
import org.neteinstein.family.domain.model.ThemeMode
import org.neteinstein.family.domain.model.UpdateCheckResult
import org.neteinstein.family.domain.repository.AppUpdateInstaller
import org.neteinstein.family.domain.usecase.CheckForUpdateUseCase
import org.neteinstein.family.domain.usecase.DownloadAppUpdateUseCase
import org.neteinstein.family.domain.usecase.GetAnalyticsEnabledUseCase
import org.neteinstein.family.domain.usecase.GetContentLanguageUseCase
import org.neteinstein.family.domain.usecase.GetLanguageOverrideUseCase
import org.neteinstein.family.domain.usecase.GetQuestionsUseCase
import org.neteinstein.family.domain.usecase.GetThemeModeUseCase
import org.neteinstein.family.domain.usecase.GetUsedQuestionIdsUseCase
import org.neteinstein.family.domain.usecase.ResetUsedQuestionsUseCase
import org.neteinstein.family.domain.usecase.SetAnalyticsEnabledUseCase
import org.neteinstein.family.domain.usecase.SetLanguageOverrideUseCase
import org.neteinstein.family.domain.usecase.SetThemeModeUseCase
import java.io.File

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {
    private val testDispatcher = StandardTestDispatcher()
    private val checkForUpdateUseCase: CheckForUpdateUseCase = mockk()
    private val downloadAppUpdateUseCase: DownloadAppUpdateUseCase = mockk()
    private val appUpdateInstaller: AppUpdateInstaller = mockk(relaxUnitFun = true)
    private val resetUsedQuestionsUseCase: ResetUsedQuestionsUseCase = mockk()
    private val themeModeFlow = MutableStateFlow(ThemeMode.SYSTEM)
    private val getThemeModeUseCase: GetThemeModeUseCase = mockk()
    private val setThemeModeUseCase: SetThemeModeUseCase = mockk(relaxUnitFun = true)
    private val getQuestionsUseCase: GetQuestionsUseCase = mockk()
    private val getUsedQuestionIdsUseCase: GetUsedQuestionIdsUseCase = mockk()
    private val getContentLanguageUseCase: GetContentLanguageUseCase = mockk()
    private val getLanguageOverrideUseCase: GetLanguageOverrideUseCase = mockk()
    private val setLanguageOverrideUseCase: SetLanguageOverrideUseCase = mockk(relaxUnitFun = true)

    private val update = AppUpdate(versionName = "1.0.6", apkDownloadUrl = "https://example.com/app.apk")
    private val questions =
        listOf(
            Question(id = 1, text = "Q1", languageCode = "en", category = QuestionCategory.IceBreakers),
            Question(id = 2, text = "Q2", languageCode = "en", category = QuestionCategory.IceBreakers),
        )

    private val getAnalyticsEnabledUseCase: GetAnalyticsEnabledUseCase = mockk()
    private val setAnalyticsEnabledUseCase: SetAnalyticsEnabledUseCase = mockk()

    // Relaxed: most of these tests are about update/reset/theme behaviour and don't care that
    // those paths now also report an event. The analytics assertions live in their own tests.
    private val analyticsTracker: AnalyticsTracker = mockk(relaxed = true)

    private lateinit var viewModel: SettingsViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        every { getThemeModeUseCase() } returns themeModeFlow
        every { getContentLanguageUseCase() } returns "en"
        every { getLanguageOverrideUseCase() } returns null
        every { getAnalyticsEnabledUseCase() } returns true
        coEvery { setAnalyticsEnabledUseCase(any()) } returns Unit
        coEvery { getQuestionsUseCase("en") } returns questions
        coEvery { getUsedQuestionIdsUseCase() } returns emptySet()
        viewModel =
            SettingsViewModel(
                checkForUpdateUseCase,
                downloadAppUpdateUseCase,
                appUpdateInstaller,
                resetUsedQuestionsUseCase,
                getThemeModeUseCase,
                setThemeModeUseCase,
                getQuestionsUseCase,
                getUsedQuestionIdsUseCase,
                getContentLanguageUseCase,
                getLanguageOverrideUseCase,
                setLanguageOverrideUseCase,
                getAnalyticsEnabledUseCase,
                setAnalyticsEnabledUseCase,
                analyticsTracker,
            )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `onScreenEntered sets UpToDate when no update is available`() =
        runTest {
            coEvery { checkForUpdateUseCase() } returns Result.success(UpdateCheckResult.UpToDate("1.0.5"))

            viewModel.onScreenEntered()
            testDispatcher.scheduler.advanceUntilIdle()

            assertEquals(UpdateStatus.UpToDate("1.0.5"), viewModel.uiState.value.updateStatus)
        }

    @Test
    fun `onScreenEntered sets UpdateAvailable when a newer release exists`() =
        runTest {
            coEvery { checkForUpdateUseCase() } returns Result.success(UpdateCheckResult.UpdateAvailable(update))

            viewModel.onScreenEntered()
            testDispatcher.scheduler.advanceUntilIdle()

            assertEquals(UpdateStatus.UpdateAvailable(update), viewModel.uiState.value.updateStatus)
        }

    @Test
    fun `onUpdateClicked downloads and installs when an update is already known`() =
        runTest {
            coEvery { checkForUpdateUseCase() } returns Result.success(UpdateCheckResult.UpdateAvailable(update))
            viewModel.onScreenEntered()
            testDispatcher.scheduler.advanceUntilIdle()

            val apkFile = File("/tmp/app.apk")
            every { appUpdateInstaller.canInstallPackages() } returns true
            coEvery { downloadAppUpdateUseCase(update) } returns Result.success(apkFile)

            viewModel.onUpdateClicked()
            testDispatcher.scheduler.advanceUntilIdle()

            verify { appUpdateInstaller.installPackage(apkFile) }
            assertEquals(UpdateStatus.Idle, viewModel.uiState.value.updateStatus)
        }

    @Test
    fun `onUpdateClicked stops at SideloadingBlocked when the OS blocks installs`() =
        runTest {
            coEvery { checkForUpdateUseCase() } returns Result.success(UpdateCheckResult.UpdateAvailable(update))
            viewModel.onScreenEntered()
            testDispatcher.scheduler.advanceUntilIdle()

            every { appUpdateInstaller.canInstallPackages() } returns false

            viewModel.onUpdateClicked()
            testDispatcher.scheduler.advanceUntilIdle()

            assertEquals(UpdateStatus.SideloadingBlocked, viewModel.uiState.value.updateStatus)
        }

    @Test
    fun `onUpdateClicked with no known update checks first then downloads`() =
        runTest {
            coEvery { checkForUpdateUseCase() } returns Result.success(UpdateCheckResult.UpdateAvailable(update))
            every { appUpdateInstaller.canInstallPackages() } returns true
            coEvery { downloadAppUpdateUseCase(update) } returns Result.success(File("/tmp/app.apk"))

            viewModel.onUpdateClicked()
            testDispatcher.scheduler.advanceUntilIdle()

            verify { appUpdateInstaller.installPackage(any()) }
        }

    @Test
    fun `onUpdateClicked surfaces a failure message when the check fails`() =
        runTest {
            coEvery { checkForUpdateUseCase() } returns Result.failure(IllegalStateException("network error"))

            viewModel.onUpdateClicked()
            testDispatcher.scheduler.advanceUntilIdle()

            val status = viewModel.uiState.value.updateStatus
            assertTrue(status is UpdateStatus.Failed)
            assertEquals("network error", (status as UpdateStatus.Failed).message)
        }

    @Test
    fun `onEnableSideloadingClicked opens the install permission settings`() {
        viewModel.onEnableSideloadingClicked()

        verify { appUpdateInstaller.openInstallPermissionSettings() }
    }

    @Test
    fun `onResetCardsClicked resets used questions and reports done`() =
        runTest {
            coEvery { resetUsedQuestionsUseCase() } returns Unit

            viewModel.onResetCardsClicked()
            testDispatcher.scheduler.advanceUntilIdle()

            coVerify { resetUsedQuestionsUseCase() }
            assertEquals(ResetCardsStatus.Done, viewModel.uiState.value.resetCardsStatus)
        }

    @Test
    fun `onResetCardsClicked zeroes out the hidden cards count`() =
        runTest {
            coEvery { getUsedQuestionIdsUseCase() } returns setOf(1)
            coEvery { resetUsedQuestionsUseCase() } returns Unit
            coEvery { checkForUpdateUseCase() } returns Result.success(UpdateCheckResult.UpToDate("1.0.5"))
            viewModel.onScreenEntered()
            testDispatcher.scheduler.advanceUntilIdle()
            assertEquals(1, viewModel.uiState.value.hiddenCardsCount)

            viewModel.onResetCardsClicked()
            testDispatcher.scheduler.advanceUntilIdle()

            assertEquals(0, viewModel.uiState.value.hiddenCardsCount)
        }

    @Test
    fun `onScreenEntered loads total and hidden card counts`() =
        runTest {
            coEvery { getUsedQuestionIdsUseCase() } returns setOf(1)
            coEvery { checkForUpdateUseCase() } returns Result.success(UpdateCheckResult.UpToDate("1.0.5"))

            viewModel.onScreenEntered()
            testDispatcher.scheduler.advanceUntilIdle()

            assertEquals(2, viewModel.uiState.value.totalCardsCount)
            assertEquals(1, viewModel.uiState.value.hiddenCardsCount)
        }

    @Test
    fun `updatesEnabled defaults to true so the Updates section shows by default`() {
        assertTrue(viewModel.uiState.value.updatesEnabled)
    }

    @Test
    fun `onScreenEntered skips the update check when updates are disabled`() =
        runTest {
            val playStoreViewModel =
                SettingsViewModel(
                    checkForUpdateUseCase,
                    downloadAppUpdateUseCase,
                    appUpdateInstaller,
                    resetUsedQuestionsUseCase,
                    getThemeModeUseCase,
                    setThemeModeUseCase,
                    getQuestionsUseCase,
                    getUsedQuestionIdsUseCase,
                    getContentLanguageUseCase,
                    getLanguageOverrideUseCase,
                    setLanguageOverrideUseCase,
                    getAnalyticsEnabledUseCase,
                    setAnalyticsEnabledUseCase,
                    analyticsTracker,
                    updatesEnabled = false,
                )

            playStoreViewModel.onScreenEntered()
            testDispatcher.scheduler.advanceUntilIdle()

            coVerify(exactly = 0) { checkForUpdateUseCase() }
            assertEquals(false, playStoreViewModel.uiState.value.updatesEnabled)
            assertEquals(UpdateStatus.Idle, playStoreViewModel.uiState.value.updateStatus)
        }

    @Test
    fun `themeMode defaults to SYSTEM`() {
        assertEquals(ThemeMode.SYSTEM, viewModel.uiState.value.themeMode)
    }

    @Test
    fun `uiState reflects the persisted theme mode`() =
        runTest {
            themeModeFlow.value = ThemeMode.DARK
            testDispatcher.scheduler.advanceUntilIdle()

            assertEquals(ThemeMode.DARK, viewModel.uiState.value.themeMode)
        }

    @Test
    fun `languageOverride defaults to null`() {
        assertEquals(null, viewModel.uiState.value.languageOverride)
    }

    @Test
    fun `onLanguageSelected persists the chosen language and updates state`() =
        runTest {
            coEvery { getQuestionsUseCase("pt") } returns questions
            every { getContentLanguageUseCase() } returns "pt"

            viewModel.onLanguageSelected(AppLanguage.PORTUGUESE)
            testDispatcher.scheduler.advanceUntilIdle()

            coVerify { setLanguageOverrideUseCase(AppLanguage.PORTUGUESE) }
            assertEquals(AppLanguage.PORTUGUESE, viewModel.uiState.value.languageOverride)
        }

    @Test
    fun `onLanguageSelected with null clears the override`() =
        runTest {
            viewModel.onLanguageSelected(null)
            testDispatcher.scheduler.advanceUntilIdle()

            coVerify { setLanguageOverrideUseCase(null) }
            assertEquals(null, viewModel.uiState.value.languageOverride)
        }

    @Test
    fun `onThemeModeSelected persists the chosen theme mode`() =
        runTest {
            viewModel.onThemeModeSelected(ThemeMode.LIGHT)
            testDispatcher.scheduler.advanceUntilIdle()

            coVerify { setThemeModeUseCase(ThemeMode.LIGHT) }
        }

    @Test
    fun `the analytics switch starts at the persisted choice`() {
        every { getAnalyticsEnabledUseCase() } returns false

        val optedOut =
            SettingsViewModel(
                checkForUpdateUseCase,
                downloadAppUpdateUseCase,
                appUpdateInstaller,
                resetUsedQuestionsUseCase,
                getThemeModeUseCase,
                setThemeModeUseCase,
                getQuestionsUseCase,
                getUsedQuestionIdsUseCase,
                getContentLanguageUseCase,
                getLanguageOverrideUseCase,
                setLanguageOverrideUseCase,
                getAnalyticsEnabledUseCase,
                setAnalyticsEnabledUseCase,
                analyticsTracker,
            )

        assertEquals(false, optedOut.uiState.value.analyticsEnabled)
    }

    /**
     * Driving the tracker before the suspending write is what makes an opt-out take effect
     * immediately rather than after a round trip to storage - the assertion on ordering is the
     * point, not an implementation detail.
     */
    @Test
    fun `opting out stops the SDK before persisting the choice`() =
        runTest {
            viewModel.onAnalyticsEnabledChanged(false)

            verify { analyticsTracker.setCollectionEnabled(false) }
            assertEquals(false, viewModel.uiState.value.analyticsEnabled)

            testDispatcher.scheduler.advanceUntilIdle()
            coVerify { setAnalyticsEnabledUseCase(false) }
        }

    @Test
    fun `opting back in restarts the SDK and persists the choice`() =
        runTest {
            viewModel.onAnalyticsEnabledChanged(true)
            testDispatcher.scheduler.advanceUntilIdle()

            verify { analyticsTracker.setCollectionEnabled(true) }
            coVerify { setAnalyticsEnabledUseCase(true) }
            assertEquals(true, viewModel.uiState.value.analyticsEnabled)
        }

    @Test
    fun `onThemeModeSelected reports the chosen mode`() =
        runTest {
            viewModel.onThemeModeSelected(ThemeMode.DARK)

            verify {
                analyticsTracker.logEvent(
                    AnalyticsEvents.THEME_CHANGED,
                    mapOf(AnalyticsParams.THEME_MODE to "dark"),
                )
            }
        }

    @Test
    fun `onLanguageSelected reports the chosen language code`() =
        runTest {
            coEvery { setLanguageOverrideUseCase(any()) } returns Unit

            viewModel.onLanguageSelected(AppLanguage.FRENCH)

            verify {
                analyticsTracker.logEvent(
                    AnalyticsEvents.LANGUAGE_CHANGED,
                    mapOf(AnalyticsParams.LANGUAGE to "fr"),
                )
            }
        }

    @Test
    fun `onLanguageSelected reports clearing the override as automatic`() =
        runTest {
            coEvery { setLanguageOverrideUseCase(any()) } returns Unit

            viewModel.onLanguageSelected(null)

            verify {
                analyticsTracker.logEvent(
                    AnalyticsEvents.LANGUAGE_CHANGED,
                    mapOf(AnalyticsParams.LANGUAGE to ANALYTICS_VALUE_AUTOMATIC),
                )
            }
        }

    @Test
    fun `onResetCardsClicked reports how many cards were hidden beforehand`() =
        runTest {
            coEvery { getUsedQuestionIdsUseCase() } returns setOf(1, 2, 3)
            coEvery { resetUsedQuestionsUseCase() } returns Unit
            // onScreenEntered is the only thing that populates hiddenCardsCount, and it also kicks
            // off the background update check - which needs a stub here even though this test is
            // about neither.
            coEvery { checkForUpdateUseCase() } returns Result.success(UpdateCheckResult.UpToDate("1.0.0"))
            viewModel.onScreenEntered()
            testDispatcher.scheduler.advanceUntilIdle()

            viewModel.onResetCardsClicked()

            verify {
                analyticsTracker.logEvent(
                    AnalyticsEvents.CARDS_RESET,
                    mapOf(AnalyticsParams.HIDDEN_COUNT_BEFORE to 3),
                )
            }
        }

    @Test
    fun `onOutboundLinkClicked reports which link was tapped`() {
        viewModel.onOutboundLinkClicked(SettingsViewModel.LINK_LOOPGAIN)

        verify {
            analyticsTracker.logEvent(
                AnalyticsEvents.OUTBOUND_LINK_CLICKED,
                mapOf(AnalyticsParams.LINK to "loopgain"),
            )
        }
    }
}
