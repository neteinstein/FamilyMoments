package org.neteinstein.family

import android.app.Application
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level
import org.neteinstein.family.data.local.AndroidAppContext
import org.neteinstein.family.di.appModule

class FamilyMomentsApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // Set before startKoin(): core:data's Android actuals (Room, SharedPreferences, the
        // GitHub self-update flow) read this directly rather than through Koin's androidContext()
        // helper, since commonMain code can never call an Android-only Koin API - see AGENTS.md's
        // KMP migration section.
        AndroidAppContext.instance = this
        startKoin {
            androidLogger(Level.ERROR)
            androidContext(this@FamilyMomentsApp)
            modules(appModule(BuildConfig.UPDATES_ENABLED))
        }
    }
}
