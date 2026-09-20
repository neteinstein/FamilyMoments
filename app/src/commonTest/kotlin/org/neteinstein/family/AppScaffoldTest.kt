package org.neteinstein.family

import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * Trivial commonTest proving the `kotlin.test` + `commonTest` wiring runs on every target
 * (`testDebugUnitTest` on Android, `:app:iosSimulatorArm64Test`/`:app:iosX64Test` on iOS,
 * `:app:wasmJsTest` on Web) independently of `app`'s real shared logic (`App()`, navigation, DI -
 * covered by MainActivityViewModelTest under `androidHostTest`, MockK-based so JVM/Android-only).
 */
class AppScaffoldTest {
    @Test
    fun `scaffold module builds and its commonTest runs`() {
        assertTrue(true)
    }
}
