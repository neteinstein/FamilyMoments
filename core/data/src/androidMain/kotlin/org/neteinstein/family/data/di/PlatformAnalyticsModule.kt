package org.neteinstein.family.data.di

import org.koin.core.module.Module
import org.koin.dsl.module
import org.neteinstein.family.data.analytics.FirebaseAnalyticsTracker
import org.neteinstein.family.domain.analytics.AnalyticsTracker

// The raw platform tracker, registered under a qualifier because the unqualified AnalyticsTracker
// the rest of the app injects is ConsentAwareAnalyticsTracker, which wraps this one - see
// DataModule.kt. Firebase degrades to a no-op by itself when google-services.json was absent at
// build time, so there is no "is Firebase configured" branch here; see FirebaseAnalyticsTracker.
actual val platformAnalyticsModule: Module =
    module {
        single<AnalyticsTracker>(platformAnalyticsDelegate) { FirebaseAnalyticsTracker() }
    }
