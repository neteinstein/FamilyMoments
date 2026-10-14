import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi

plugins {
    id("familymoments.kmp.compose.library")
}

// The shared Kotlin Multiplatform aggregator module - commonMain holds the composed Koin
// `appModule`, `App()` (theme + navigation), and the platform entry points androidApp/iosApp/
// webApp each wrap in a few lines of platform glue. See AGENTS.md's KMP migration section and
// neteinstein/loopgain's `composeApp` module, which this mirrors (kept named `app` since that's
// this repo's existing module name).
kotlin {
    @OptIn(ExperimentalKotlinGradlePluginApi::class)
    android {
        namespace = "org.neteinstein.family.shared"

        withHostTestBuilder {}.configure {}
    }

    // Re-invokes the iosArm64()/iosSimulatorArm64() targets familymoments.kmp.library already
    // declared, purely to reach their `binaries.framework { }` - reconfiguring an already-declared
    // KMP target this way (rather than re-declaring it) is the standard way a convention plugin's
    // consumer adds target-specific config on top of the shared shape.
    listOf(
        iosArm64(),
        iosSimulatorArm64(),
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "FamilyMomentsShared"
            isStatic = true
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(project(":core:domain"))
            implementation(project(":core:data"))
            implementation(project(":core:ui"))
            implementation(project(":feature:splash"))
            implementation(project(":feature:home"))
            implementation(project(":feature:settings"))
            // Compose Material3/Foundation/animation/runtime and lifecycle-runtime-compose all
            // reach this module transitively via core:ui's `api` exports (the same pattern every
            // feature:* module already uses) - no need to redeclare them here.
            implementation(libs.koin.core)
            implementation(libs.koin.compose)
            implementation(libs.koin.compose.viewmodel)
            // The JetBrains-published KMP fork, not the mainline androidx.navigation:navigation-
            // compose artifact - same androidx.navigation.* package (NavHost, composable,
            // rememberNavController - no source changes needed), but the mainline artifact
            // publishes no wasmJs variant at all, confirmed by a real Gradle resolution failure.
            implementation(libs.navigation.compose.multiplatform)
        }

        androidMain.dependencies {
            implementation(libs.koin.android)
        }

        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }

        getByName("androidHostTest") {
            dependencies {
                implementation(libs.junit)
                implementation(libs.mockk)
                implementation(libs.coroutines.test)
            }
        }
    }
}
