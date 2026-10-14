import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi

plugins {
    id("familymoments.kmp.compose.library")
}

kotlin {
    @OptIn(ExperimentalKotlinGradlePluginApi::class)
    android {
        namespace = "org.neteinstein.family.feature.splash"

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
