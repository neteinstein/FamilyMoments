import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.kotlin.multiplatform.library)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ktlint)
    alias(libs.plugins.kover)
}

kotlin {
    jvmToolchain(17)

    @OptIn(ExperimentalKotlinGradlePluginApi::class)
    android {
        namespace = "org.neteinstein.family.feature.splash"
        compileSdk = 37
        minSdk = 32

        // Compose Multiplatform resources (compose.resources { } below) reach the Android target
        // as Android assets - off by default for a KMP android-library module (unlike a classic
        // android-library, where it's implicit), so without this stringResource() calls can't find
        // anything at runtime.
        androidResources {
            enable = true
        }
    }

    iosArm64()
    iosSimulatorArm64()

    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser()
    }

    sourceSets {
        commonMain.dependencies {
            implementation(project(":core:ui"))
            // Declared directly (not just inherited transitively via core:ui's `api`) because the
            // Compose resource generator only wires up generation for this module's own
            // composeResources/ (values/strings.xml below) when the module declares this itself -
            // core:ui's equivalent declaration only activates generation for core:ui's resources.
            implementation(compose.components.resources)
        }
    }
}

compose.resources {
    packageOfResClass = "org.neteinstein.family.feature.splash.resources"
}

ktlint {
    filter {
        exclude { entry -> entry.file.path.contains("${File.separatorChar}generated${File.separatorChar}") }
    }
    reporters {
        reporter(org.jlleitschuh.gradle.ktlint.reporter.ReporterType.PLAIN)
        reporter(org.jlleitschuh.gradle.ktlint.reporter.ReporterType.CHECKSTYLE)
    }
}
