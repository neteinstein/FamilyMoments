package org.neteinstein.family.data.local

import android.content.Context

/**
 * Set once, in `FamilyMomentsApp.onCreate()`, before `initKoin()` runs - lets every Android actual
 * in this module reach an app [Context] without threading it through Koin (Koin's `androidContext()`
 * helper is Android-only, so commonMain code can never call it). Same pattern as
 * neteinstein/loopgain's `AndroidAppContext`.
 */
object AndroidAppContext {
    lateinit var instance: Context
}
