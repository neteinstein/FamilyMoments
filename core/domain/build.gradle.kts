import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.kotlin.multiplatform.library)
    alias(libs.plugins.ktlint)
    alias(libs.plugins.kover)
}

// Pure Kotlin domain layer (models, repository interfaces, use cases) - see AGENTS.md's KMP
// migration section. The GitHub self-update feature (Android-only - APK sideloading has no
// iOS/Web equivalent) is common code too, aside from PlatformFile's `java.io.File` actual
// (androidMain) - the interfaces/use cases are shared, only the concrete
// UpdateRepository/AppUpdateInstaller implementations differ per platform (core:data).
kotlin {
    jvmToolchain(17)

    @OptIn(ExperimentalKotlinGradlePluginApi::class)
    android {
        namespace = "org.neteinstein.family.domain"
        compileSdk = 37
        minSdk = 32

        withHostTestBuilder {}.configure {}
    }

    iosArm64()
    iosSimulatorArm64()

    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser()
    }

    sourceSets {
        commonMain.dependencies {
            implementation(libs.coroutines.core)
        }

        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.coroutines.test)
        }

        // androidHostTest is created dynamically by withHostTestBuilder{} above, so (unlike the
        // static commonMain/commonTest) it has no generated typesafe accessor - reached via
        // getByName instead.
        getByName("androidHostTest") {
            dependencies {
                implementation(libs.junit)
                implementation(libs.mockk)
                implementation(libs.coroutines.test)
            }
        }
    }
}

ktlint {
    reporters {
        reporter(org.jlleitschuh.gradle.ktlint.reporter.ReporterType.PLAIN)
        reporter(org.jlleitschuh.gradle.ktlint.reporter.ReporterType.CHECKSTYLE)
    }
}
