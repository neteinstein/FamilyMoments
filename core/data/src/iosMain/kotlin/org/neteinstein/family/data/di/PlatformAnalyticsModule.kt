package org.neteinstein.family.data.di

import org.koin.core.module.Module
import org.koin.dsl.module
import org.neteinstein.family.data.analytics.NoOpAnalyticsTracker
import org.neteinstein.family.domain.analytics.AnalyticsTracker

// No Firebase app is registered for iOS on this project: the google-services.json this feature was
// built from declares Android clients only, and iosApp carries no GoogleService-Info.plist or
// Firebase SDK. This binding exists so Koin can satisfy injection, exactly like
// platformUpdateModule's iOS actual - adding iOS analytics later means registering an iOS app in
// the Firebase console and replacing this one binding.
actual val platformAnalyticsModule: Module =
    module {
        single<AnalyticsTracker>(platformAnalyticsDelegate) { NoOpAnalyticsTracker() }
    }
