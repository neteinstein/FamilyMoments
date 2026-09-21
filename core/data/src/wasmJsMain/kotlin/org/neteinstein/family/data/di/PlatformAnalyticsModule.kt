package org.neteinstein.family.data.di

import org.koin.core.module.Module
import org.koin.dsl.module
import org.neteinstein.family.data.analytics.FirebaseWebAnalyticsTracker
import org.neteinstein.family.domain.analytics.AnalyticsTracker

// See the Android actual for why this is qualified. The web tracker no-ops by itself when the
// build rendered no firebase-init.js - see FirebaseWebAnalyticsTracker.
actual val platformAnalyticsModule: Module =
    module {
        single<AnalyticsTracker>(platformAnalyticsDelegate) { FirebaseWebAnalyticsTracker() }
    }
