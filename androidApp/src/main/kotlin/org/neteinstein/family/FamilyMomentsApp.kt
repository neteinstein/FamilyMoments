package org.neteinstein.family

import android.app.Application
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level
import org.neteinstein.family.di.appModule
import org.neteinstein.family.di.setAndroidAppContext

class FamilyMomentsApp : Application() {
    override fun onCreate() {
        super.onCreate()
        setAndroidAppContext(this)
        startKoin {
            androidLogger(Level.ERROR)
            androidContext(this@FamilyMomentsApp)
            modules(appModule(BuildConfig.UPDATES_ENABLED, BuildConfig.DISTRIBUTION))
        }
    }
}
