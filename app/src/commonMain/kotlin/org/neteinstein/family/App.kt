package org.neteinstein.family

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

/**
 * Shared entry point composable, called from `MainActivity` (androidApp), `MainViewController`
 * (iosApp, via `app`'s iosMain) and `main()` (webApp).
 *
 * Placeholder for now - the real theme + navigation graph (today's `app/.../navigation/
 * AppNavigation.kt` on the pre-KMP `androidApp` module) moves here once `core:domain`, `core:data`,
 * `core:ui` and `feature:*` have each been converted to Kotlin Multiplatform library modules (see
 * AGENTS.md's KMP migration phasing) - this module can't depend on them yet since they're still
 * Android-only `android-library` modules today. Its only job right now is proving the KMP + Compose
 * Multiplatform module/target wiring builds and renders on Android, iOS and Web in isolation,
 * without touching (or risking breaking) the real, still fully Android-only app in `androidApp`.
 */
@Composable
fun App() {
    MaterialTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("FamilyMoments (KMP scaffold)")
            }
        }
    }
}
