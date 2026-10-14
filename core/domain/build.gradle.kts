import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi

plugins {
    id("familymoments.kmp.library")
}

// Pure Kotlin domain layer (models, repository interfaces, use cases) - see AGENTS.md's KMP
// migration section. The GitHub self-update feature (Android-only - APK sideloading has no
// iOS/Web equivalent) is common code too, aside from PlatformFile's `java.io.File` actual
// (androidMain) - the interfaces/use cases are shared, only the concrete
// UpdateRepository/AppUpdateInstaller implementations differ per platform (core:data).
kotlin {
    @OptIn(ExperimentalKotlinGradlePluginApi::class)
    android {
        namespace = "org.neteinstein.family.domain"

        withHostTestBuilder {}.configure {}
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
