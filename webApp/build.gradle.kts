import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ktlint)
}

// The Web (wasmJs) entry point - mirrors androidApp/iosApp's role for Android/iOS: a thin
// platform-specific wrapper around `app`'s shared `App()` composable. See AGENTS.md's KMP
// migration section.
kotlin {
    jvmToolchain(17)

    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser {
            commonWebpackConfig {
                outputFileName = "familymoments.js"
            }
        }
        binaries.executable()
    }

    sourceSets {
        wasmJsMain.dependencies {
            implementation(projects.app)
            // `implementation(projects.app)` isn't transitive, so app's own compose deps don't
            // leak into webApp's compile classpath - ComposeViewport needs its own dependency
            // here regardless. Referenced by coordinate rather than the `compose.ui` version
            // catalog accessor, which is deprecated in this Compose Multiplatform version.
            implementation("org.jetbrains.compose.ui:ui:${libs.versions.composeMultiplatform.get()}")
        }
    }
}

ktlint {
    reporters {
        reporter(org.jlleitschuh.gradle.ktlint.reporter.ReporterType.PLAIN)
        reporter(org.jlleitschuh.gradle.ktlint.reporter.ReporterType.CHECKSTYLE)
    }
}
