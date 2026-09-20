package org.neteinstein.family

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.rememberNavController
import org.koin.compose.viewmodel.koinViewModel
import org.neteinstein.family.domain.model.ThemeMode
import org.neteinstein.family.navigation.AppNavigation
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
    val darkTheme =
        when (themeMode) {
            ThemeMode.LIGHT -> false
            ThemeMode.DARK -> true
            ThemeMode.SYSTEM -> isSystemInDarkTheme()
        }

    FamilyMomentsTheme(darkTheme = darkTheme, dynamicColor = false) {
        val navController = rememberNavController()
        AppNavigation(navController = navController)
    }
}
