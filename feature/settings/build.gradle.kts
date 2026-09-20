import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi

plugins {
    id("familymoments.kmp.compose.library")
}

kotlin {
    @OptIn(ExperimentalKotlinGradlePluginApi::class)
    android {
        namespace = "org.neteinstein.family.feature.settings"

        withHostTestBuilder {}.configure {}

        // Compose Multiplatform resources (compose.resources { } below) reach the Android target
        // as Android assets - off by default for a KMP android-library module (unlike a classic
        // android-library, where it's implicit), so without this stringResource() calls can't find
        // anything at runtime.
        androidResources {
            enable = true
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(project(":core:domain"))
            implementation(project(":core:ui"))
            implementation(compose.components.resources)
            implementation(libs.koin.compose)
            implementation(libs.koin.compose.viewmodel)
            implementation(libs.coroutines.core)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.coroutines.test)
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

compose.resources {
    packageOfResClass = "org.neteinstein.family.feature.settings.resources"
}
