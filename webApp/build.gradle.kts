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
            // compose.ui comes in transitively via `app` (compose.material3/compose.foundation);
            // no need to declare it again here (a direct `compose.ui` accessor is deprecated).
            implementation(projects.app)
        }
    }
}

ktlint {
    reporters {
        reporter(org.jlleitschuh.gradle.ktlint.reporter.ReporterType.PLAIN)
        reporter(org.jlleitschuh.gradle.ktlint.reporter.ReporterType.CHECKSTYLE)
    }
}
