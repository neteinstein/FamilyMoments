import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi

plugins {
    id("familymoments.kmp.compose.library")
}

// Shared Compose Multiplatform theme (Color/Theme/Typography) + the campfire artwork - see
// AGENTS.md's KMP migration section. Exposes Compose libs via `api` so feature modules keep
// getting them transitively without declaring their own dependency, same as before this
// conversion.
kotlin {
    @OptIn(ExperimentalKotlinGradlePluginApi::class)
    android {
        namespace = "org.neteinstein.family.ui"

        // Compose Multiplatform resources (compose.resources { } below, the campfire drawables)
        // reach the Android target as Android assets - off by default for a KMP android-library
        // module (unlike a classic android-library, where it's implicit), so without this
        // painterResource() calls can't find anything at runtime.
        androidResources {
            enable = true
        }

        // ProvideAppLanguageTest renders real Compose UI (and real composeResources lookups) via
        // Robolectric - needs Android resources on the test classpath, same as feature:home's
        // screen tests.
        withHostTestBuilder {}.configure {
            isIncludeAndroidResources = true
        }
    }

    sourceSets {
        commonMain.dependencies {
            api(compose.runtime)
            api(compose.foundation)
            api(compose.material3)
            api(compose.materialIconsExtended)
            api(compose.animation)
            api(compose.components.resources)
            api(compose.components.uiToolingPreview)
            // lifecycle-viewmodel-compose (unlike lifecycle-runtime-compose above) only publishes
            // Android/JVM variants as of 2.10.0 - no wasm-js artifact - so it can't sit on this
            // module's commonMain api surface. Feature modules that need Compose-scoped ViewModels
            // use Koin's koin-compose-viewmodel (koinViewModel()) instead, which is KMP-native.
            api(libs.lifecycle.runtime.compose)
        }
        getByName("androidHostTest") {
            dependencies {
                implementation(libs.junit)
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
        androidMain.dependencies {
            // WindowCompat (PlatformTheme.android.kt's status-bar icon appearance actual) lives
            // here - this module used to get it transitively as a classic android-library; the
            // KMP android source set needs it declared directly.
            implementation(libs.core.ktx)
        }
    }
}

compose.resources {
    packageOfResClass = "org.neteinstein.family.ui.resources"
    // Generated resource accessors (Res.drawable.*) default to module-internal visibility -
    // feature:splash (a different module) needs to reach them for the campfire artwork.
    publicResClass = true
}
