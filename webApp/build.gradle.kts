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

    wasmJs {
        moduleName = "familymoments"
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
            implementation(compose.ui)
        }
    }
}

ktlint {
    reporters {
        reporter(org.jlleitschuh.gradle.ktlint.reporter.ReporterType.PLAIN)
        reporter(org.jlleitschuh.gradle.ktlint.reporter.ReporterType.CHECKSTYLE)
    }
}
