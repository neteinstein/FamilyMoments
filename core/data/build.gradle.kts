import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi

plugins {
    id("familymoments.kmp.library")
    alias(libs.plugins.ksp)
}

// Repository implementations + data sources - see AGENTS.md's KMP migration section.
//
// Room stays Android-only for now, exactly as it worked before this migration (entities/DAOs live
// in androidMain, unchanged): its KMP support needs a per-target SQLite driver this module hasn't
// set up for iOS yet. iOS and wasmJs get a simple in-memory QuestionLocalDataSource actual
// instead - see that interface's doc comment. The GitHub self-update feature is Android-only
// outright (APK sideloading has no iOS/Web equivalent) - see platformUpdateModule's actuals.
kotlin {
    @OptIn(ExperimentalKotlinGradlePluginApi::class)
    android {
        namespace = "org.neteinstein.family.data"

        withHostTestBuilder {}.configure {}
    }

    sourceSets {
        commonMain.dependencies {
            implementation(project(":core:domain"))
            implementation(libs.coroutines.core)
            implementation(libs.koin.core)
        }

        androidMain.dependencies {
            implementation(libs.koin.android)
            implementation(libs.room.runtime)
            implementation(libs.room.ktx)
            // Firebase Analytics lives here, with the rest of this module's platform actuals,
            // rather than in androidApp - only the google-services *plugin* needs to sit on the
            // application module (it generates the google_app_id string resource that
            // FirebaseInitProvider reads from the merged app resources at startup). The BOM keeps
            // every com.google.firebase artifact on one mutually-compatible version.
            implementation(project.dependencies.platform(libs.firebase.bom))
            implementation(libs.firebase.analytics)
        }

        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.coroutines.test)
        }

        // androidHostTest is created dynamically by withHostTestBuilder{} above, so (unlike the
        // static commonMain/commonTest) it has no generated typesafe accessor - reached via
        // getByName, matching core:domain's build.gradle.kts.
        getByName("androidHostTest") {
            dependencies {
                implementation(libs.junit)
                implementation(libs.mockk)
                implementation(libs.coroutines.test)
                implementation(libs.koin.test)
                implementation(libs.koin.test.junit4)
                // android.jar's org.json classes are compile-only stubs (real bodies throw/return
                // defaults) - GitHubUpdateRepositoryImplTest exercises real JSONObject/JSONArray
                // parsing, so it needs a real desktop implementation of the same org.json package
                // on the test runtime classpath.
                implementation(libs.json)
            }
        }
    }
}

// Room's KSP codegen only runs against androidMain (see the kotlin{} block's comment) - Kotlin
// Multiplatform's KSP integration needs this applied per-target rather than the single-target
// ksp(...) dependency-configuration shorthand.
dependencies {
    add("kspAndroid", libs.room.compiler)
}
