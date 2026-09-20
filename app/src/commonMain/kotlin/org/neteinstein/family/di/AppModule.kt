package org.neteinstein.family.di

import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module
import org.neteinstein.family.MainActivityViewModel
import org.neteinstein.family.data.di.dataModule
import org.neteinstein.family.feature.home.di.homeModule
import org.neteinstein.family.feature.settings.di.settingsModule

/**
 * [updatesEnabled] gates the GitHub self-update feature (see feature:settings' SettingsViewModel/
 * SettingsScreen) - true only for androidApp's "github" flavor; every other caller (androidApp's
 * "playstore" flavor, iOS, Web) has no equivalent feature and passes false. Unlike androidApp,
 * `app` has no build flavors of its own, so this can't be read from a local BuildConfig - it must
 * be supplied by the caller (see [doInitKoin] and androidApp's FamilyMomentsApp).
 */
fun appModule(updatesEnabled: Boolean): Module =
    module {
        single(named("updatesEnabled")) { updatesEnabled }

        viewModel { MainActivityViewModel(get()) }

        includes(dataModule, homeModule, settingsModule)
    }
