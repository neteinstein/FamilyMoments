package org.neteinstein.family.di

import org.koin.core.KoinApplication
import org.koin.core.context.startKoin

/**
 * Starts Koin for platforms with no self-update feature to gate and no extra platform module to
 * register (iOS, Web) - called from webApp's `main()` and from iosApp's iOSApp.swift as
 * `InitKoinKt.doInitKoin()`.
 *
 * Named `doInitKoin`, not `initKoin`: Kotlin/Native's Objective-C exporter treats a top-level
 * function starting with `init` as an initializer and renames it unpredictably. androidApp calls
 * `startKoin` directly instead (see FamilyMomentsApp) since it needs to pass its own
 * `BuildConfig.UPDATES_ENABLED` and register `androidContext()`/`androidLogger()` - Kotlin/
 * Native's Objective-C framework export doesn't carry Kotlin default parameter values through to
 * Swift, so this stays genuinely zero-argument rather than defaulted.
 */
fun doInitKoin(): KoinApplication =
    startKoin {
        modules(appModule(updatesEnabled = false))
    }
