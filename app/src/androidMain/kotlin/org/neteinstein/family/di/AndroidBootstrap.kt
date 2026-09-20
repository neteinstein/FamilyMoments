package org.neteinstein.family.di

import android.content.Context
import org.neteinstein.family.data.local.AndroidAppContext

/**
 * Sets the Android [Context] that core:data's Android actuals (Room, SharedPreferences, the
 * GitHub self-update flow) read from directly rather than through Koin's `androidContext()`
 * helper, since commonMain code can never call an Android-only Koin API. Must be called before
 * [appModule] resolves anything, from `FamilyMomentsApp.onCreate()`.
 *
 * Lives here (not called from androidApp directly against `core:data`) so androidApp's own code
 * never needs a direct dependency on `core:data` - see AGENTS.md's KMP migration section.
 */
fun setAndroidAppContext(context: Context) {
    AndroidAppContext.instance = context
}
