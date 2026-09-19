package org.neteinstein.family

import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * Trivial commonTest to prove the `kotlin.test` + `commonTest` wiring runs on every target
 * (`testDebugUnitTest` on Android, `:app:iosSimulatorArm64Test`/`:app:iosX64Test` on iOS,
 * `:app:wasmJsTest` on Web) before real shared logic lands here in a later migration phase.
 */
class AppScaffoldTest {
    @Test
    fun `scaffold module builds and its commonTest runs`() {
        assertTrue(true)
    }
}
