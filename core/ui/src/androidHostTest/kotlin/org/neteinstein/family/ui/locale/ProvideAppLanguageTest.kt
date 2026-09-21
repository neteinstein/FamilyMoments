package org.neteinstein.family.ui.locale

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.core.app.ApplicationProvider
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.setResourceReaderAndroidContext
import org.jetbrains.compose.resources.stringResource
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.neteinstein.family.ui.resources.Res
import org.neteinstein.family.ui.resources.install_app_banner_action
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Renders a real `stringResource` lookup under [ProvideAppLanguage] to prove the in-app language
 * override actually reaches Compose Multiplatform's resource resolution - the whole point of that
 * composable, and the thing that breaks silently (back to the OS locale, the bug this fixed) if a
 * CMP upgrade changes the internal `LocalComposeEnvironment` seam it hooks into.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], qualifiers = "w411dp-h891dp")
class ProvideAppLanguageTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    // Same Robolectric/composeResources workaround feature:home's screen tests need - the Context
    // that resource lookups normally get from an auto-registered ContentProvider isn't there under
    // Robolectric (robolectric/robolectric#9603).
    @OptIn(ExperimentalResourceApi::class)
    @Before
    fun setUpComposeResourcesContext() {
        setResourceReaderAndroidContext(ApplicationProvider.getApplicationContext())
    }

    @Test
    fun `an override resolves string resources in the chosen language`() {
        setContentWithLanguage("pt")

        composeTestRule.onNodeWithText("Instalar app").assertExists()
    }

    @Test
    fun `switching the override re-resolves the strings already on screen`() {
        val languageCode = mutableStateOf<String?>("pt")
        composeTestRule.setContent {
            ProvideAppLanguage(languageCode = languageCode.value) {
                LocalizedLabel()
            }
        }
        composeTestRule.onNodeWithText("Instalar app").assertExists()

        languageCode.value = "de"
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("App installieren").assertExists()
    }

    @Test
    fun `no override leaves resources following the platform locale`() {
        // Robolectric's default locale is en-US, so the default (values/) strings are expected.
        setContentWithLanguage(null)

        composeTestRule.onNodeWithText("Install app").assertExists()
    }

    private fun setContentWithLanguage(languageCode: String?) {
        composeTestRule.setContent {
            ProvideAppLanguage(languageCode = languageCode) {
                LocalizedLabel()
            }
        }
        composeTestRule.waitForIdle()
    }

    @Composable
    private fun LocalizedLabel() {
        Text(text = stringResource(Res.string.install_app_banner_action))
    }
}
