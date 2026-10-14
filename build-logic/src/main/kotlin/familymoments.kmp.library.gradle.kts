import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    id("familymoments.ktlint")
    id("org.jetbrains.kotlin.multiplatform")
    id("com.android.kotlin.multiplatform.library")
    id("org.jetbrains.kotlinx.kover")
}

// Shared shape every core:*/feature:*/app KMP module targets: Android + iOS + wasmJs, plus the
// compileSdk/minSdk pair every android{} block below repeats. Each consuming module's own
// build.gradle.kts reopens `kotlin { android { ... } }` to add only what's specific to it
// (namespace, host-test config, source set dependencies) - see AGENTS.md's KMP migration section.
kotlin {
    jvmToolchain(17)

    @OptIn(ExperimentalKotlinGradlePluginApi::class)
    android {
        compileSdk = 37
        minSdk = 32
    }

    iosArm64()
    iosSimulatorArm64()

    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser()
    }
}
