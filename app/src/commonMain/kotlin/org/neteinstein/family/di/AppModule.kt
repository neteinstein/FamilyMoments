package org.neteinstein.family.di

import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module
import org.neteinstein.family.MainActivityViewModel
import org.neteinstein.family.analytics.analyticsPlatformName
import org.neteinstein.family.data.di.dataModule
import org.neteinstein.family.feature.home.di.homeModule
import org.neteinstein.family.feature.settings.di.settingsModule

/** Qualifier for the `distribution` analytics user property - see [appModule]'s `distribution`. */
internal val distributionQualifier = named("distribution")

/**
 * [updatesEnabled] gates the GitHub self-update feature (see feature:settings' SettingsViewModel/
 * SettingsScreen) - true only for androidApp's "github" flavor; every other caller (androidApp's
 * "playstore" flavor, iOS, Web) has no equivalent feature and passes false. Unlike androidApp,
 * `app` has no build flavors of its own, so this can't be read from a local BuildConfig - it must
 * be supplied by the caller (see [doInitKoin] and androidApp's FamilyMomentsApp).
 *
 * [distribution] is reported as an analytics user property and travels the same way for the same
 * reason - only androidApp's BuildConfig knows whether this is the "github" or "playstore" build.
 * It is null on iOS and Web, where the concept doesn't exist, and the property is then left unset
 * rather than filled with a placeholder. The `platform` property it pairs with needs no parameter:
 * it comes from [analyticsPlatformName]'s per-target actual.
 */
fun appModule(
    updatesEnabled: Boolean,
    distribution: String? = null,
): Module =
    module {
        single(named("updatesEnabled")) { updatesEnabled }
        if (distribution != null) {
            single(distributionQualifier) { distribution }
        }

        viewModel {
            MainActivityViewModel(
                getThemeModeUseCase = get(),
                getLanguageOverrideUseCase = get(),
                observeLanguageOverrideUseCase = get(),
                getContentLanguageUseCase = get(),
                analyticsTracker = get(),
                platform = analyticsPlatformName,
                distribution = getOrNull(distributionQualifier),
            )
        }

        includes(dataModule, homeModule, settingsModule)
    }
