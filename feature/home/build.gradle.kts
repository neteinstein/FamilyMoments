import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi

plugins {
    id("familymoments.kmp.compose.library")
}

kotlin {
    @OptIn(ExperimentalKotlinGradlePluginApi::class)
    android {
        namespace = "org.neteinstein.family.feature.home"

        // HomeScreenCategoryDropdownTest/HomeScreenGridViewTest render real Compose UI via
        // Robolectric - needs Android resources on the test classpath, same as the classic
        // android-library `testOptions.unitTests.isIncludeAndroidResources` this replaces.
        withHostTestBuilder {}.configure {
            isIncludeAndroidResources = true
        }

        // Compose Multiplatform resources (compose.resources { } below) reach the Android target
        // as Android assets - off by default for a KMP android-library module (unlike a classic
        // android-library, where it's implicit), so without this stringResource()/painterResource()
        // calls can't find anything at runtime, in Robolectric tests or the real app alike.
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
        androidMain.dependencies {
            // PlatformBackHandler's Android actual (BackHandler) lives here.
            implementation(libs.activity.compose)
        }
        getByName("androidHostTest") {
            dependencies {
                implementation(libs.junit)
                implementation(libs.mockk)
                implementation(libs.coroutines.test)
                implementation(libs.koin.test)
                implementation(libs.koin.test.junit4)
                implementation(libs.robolectric)
                implementation(libs.junit.ext)
                // KotlinDependencyHandler (this block's receiver) has no platform() shorthand of
                // its own, unlike a classic dependencies {} block - go through project.dependencies
                // directly to get Gradle's BOM/platform semantics.
                implementation(project.dependencies.platform(libs.compose.bom))
                implementation(libs.compose.ui.test.junit4)
                implementation(libs.compose.ui.test.manifest)
            }
        }
    }
}

compose.resources {
    packageOfResClass = "org.neteinstein.family.feature.home.resources"
}
