package org.neteinstein.family.di

import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module
import org.neteinstein.family.BuildConfig
import org.neteinstein.family.MainActivityViewModel
import org.neteinstein.family.data.di.dataModule
import org.neteinstein.family.feature.home.di.homeModule
import org.neteinstein.family.feature.settings.di.settingsModule

val appModule =
    module {
        // Flavor-gated: true for "github" (the direct-APK build that self-updates from GitHub
        // Releases), false for "playstore" (which the Play Store itself updates) - see
        // app/build.gradle.kts productFlavors and feature/settings's SettingsViewModel/SettingsScreen.
        single(named("updatesEnabled")) { BuildConfig.UPDATES_ENABLED }

        viewModel { MainActivityViewModel(get()) }

        includes(dataModule, homeModule, settingsModule)
    }
