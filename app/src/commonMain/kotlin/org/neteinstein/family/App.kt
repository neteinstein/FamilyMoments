package org.neteinstein.family

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.rememberNavController
import org.koin.compose.viewmodel.koinViewModel
import org.neteinstein.family.domain.analytics.AnalyticsScreens
import org.neteinstein.family.domain.model.ThemeMode
import org.neteinstein.family.navigation.AppNavigation
import org.neteinstein.family.navigation.Screen
import org.neteinstein.family.ui.components.InstallAppBanner
import org.neteinstein.family.ui.locale.ProvideAppLanguage
import org.neteinstein.family.ui.theme.FamilyMomentsTheme

/**
 * Shared entry point composable, called from `MainActivity` (androidApp), `MainViewController`
 * (iosApp, via `app`'s iosMain) and `main()` (webApp). Koin must already be started (see
 * [org.neteinstein.family.di.doInitKoin] and androidApp's `FamilyMomentsApp`) before this
 * composes, since [koinViewModel] resolves [MainActivityViewModel] from it.
 */
@Composable
fun App() {
    val viewModel: MainActivityViewModel = koinViewModel()
    val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
    val languageOverride by viewModel.languageOverride.collectAsStateWithLifecycle()
    val darkTheme =
        when (themeMode) {
            ThemeMode.LIGHT -> false
            ThemeMode.DARK -> true
            ThemeMode.SYSTEM -> isSystemInDarkTheme()
        }

    // Wraps everything so the in-app language choice (Settings' picker, iOS/Web only - Android
    // goes through the OS's own per-app language settings) applies to the app's own strings and
    // not just the question cards.
    ProvideAppLanguage(languageCode = languageOverride?.code) {
        FamilyMomentsTheme(darkTheme = darkTheme, dynamicColor = false) {
            Column {
                InstallAppBanner(onBannerAction = viewModel::onInstallBannerAction)
                Box(modifier = Modifier.weight(1f)) {
                    val navController = rememberNavController()
                    // One screen-tracking seam for all three platforms, rather than a call in
                    // each screen composable. Only the three real destinations pass through here;
                    // the grid and the full-screen card are local UI state inside HomeScreen, not
                    // routes, so they report themselves (see AnalyticsScreens).
                    LaunchedEffect(navController) {
                        navController.currentBackStackEntryFlow.collect { entry ->
                            when (entry.destination.route) {
                                Screen.Splash.route -> viewModel.onScreenViewed(AnalyticsScreens.SPLASH)
                                Screen.Home.route -> viewModel.onScreenViewed(AnalyticsScreens.HOME)
                                Screen.Settings.route -> viewModel.onScreenViewed(AnalyticsScreens.SETTINGS)
                                else -> Unit
                            }
                        }
                    }
                    AppNavigation(navController = navController)
                }
            }
        }
    }
}
