package org.neteinstein.family

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.getValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.rememberNavController
import org.koin.androidx.compose.koinViewModel
import org.neteinstein.family.domain.model.ThemeMode
import org.neteinstein.family.navigation.AppNavigation
import org.neteinstein.family.ui.theme.FamilyMomentsTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
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
    }
}
