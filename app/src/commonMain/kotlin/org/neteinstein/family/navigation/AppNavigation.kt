package org.neteinstein.family.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import org.neteinstein.family.feature.home.HomeScreen
import org.neteinstein.family.feature.settings.SettingsScreen
import org.neteinstein.family.feature.splash.SplashScreen

private const val NAV_TRANSITION_DURATION_MS = 350

@Composable
fun AppNavigation(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route,
    ) {
        composable(Screen.Splash.route) {
            SplashScreen(
                onSplashFinished = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                },
            )
        }
        composable(Screen.Home.route) {
            HomeScreen(
                onSettingsClick = {
                    navController.navigate(Screen.Settings.route)
                },
            )
        }
        composable(
            Screen.Settings.route,
            enterTransition = {
                slideInHorizontally(tween(NAV_TRANSITION_DURATION_MS)) { it } +
                    fadeIn(tween(NAV_TRANSITION_DURATION_MS))
            },
            exitTransition = {
                slideOutHorizontally(tween(NAV_TRANSITION_DURATION_MS)) { -it / 4 } +
                    fadeOut(tween(NAV_TRANSITION_DURATION_MS))
            },
            popEnterTransition = {
                slideInHorizontally(tween(NAV_TRANSITION_DURATION_MS)) { -it / 4 } +
                    fadeIn(tween(NAV_TRANSITION_DURATION_MS))
            },
            popExitTransition = {
                slideOutHorizontally(tween(NAV_TRANSITION_DURATION_MS)) { it } +
                    fadeOut(tween(NAV_TRANSITION_DURATION_MS))
            },
        ) {
            SettingsScreen(
                onBack = {
                    navController.navigateUp()
                },
            )
        }
    }
}
