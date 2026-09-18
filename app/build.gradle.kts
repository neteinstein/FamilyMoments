import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.kotlin.multiplatform.library)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ktlint)
}

// The shared Kotlin Multiplatform aggregator module - commonMain holds the composed Koin
// `appModule`, `App()` (theme + navigation), and the platform entry points androidApp/iosApp/
// webApp each wrap in a few lines of platform glue. See AGENTS.md's KMP migration section and
// neteinstein/loopgain's `composeApp` module, which this mirrors (kept named `app` since that's
// this repo's existing module name).
kotlin {
    jvmToolchain(17)

    @OptIn(ExperimentalKotlinGradlePluginApi::class)
    android {
        namespace = "org.neteinstein.family.shared"
        compileSdk = 37
        minSdk = 32

        withHostTestBuilder {}.configure {}
    }

    listOf(
        iosArm64(),
        iosSimulatorArm64(),
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "FamilyMomentsShared"
            isStatic = true
        }
    }

    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser()
    }

    sourceSets {
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            // compose.material3/compose.foundation already pull in UI transitively; a direct
            // compose.ui accessor is deprecated in this Compose Multiplatform version.
            // compose.components.resources isn't added yet: nothing here references generated
            // Res.* yet, and its resource-generator task emits generated Kotlin that ktlint then
            // lints (and fails on) since nothing excludes app/build/generated from its scan - add
            // it back, with that exclusion, once a later phase actually needs Compose resources.
            implementation(compose.components.uiToolingPreview)
            implementation(libs.koin.core)
            implementation(libs.koin.compose)
        }

        androidMain.dependencies {
            implementation(libs.koin.android)
        }

        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}

ktlint {
    reporters {
        reporter(org.jlleitschuh.gradle.ktlint.reporter.ReporterType.PLAIN)
        reporter(org.jlleitschuh.gradle.ktlint.reporter.ReporterType.CHECKSTYLE)
    }
}
