plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    id("familymoments.ktlint")
    alias(libs.plugins.play.publisher)
}

// The Firebase/google-services plugin is applied conditionally rather than declared in the
// `plugins { }` block above (where it would be unconditional): it hard-fails the build with
// "File google-services.json is missing" when that file is absent, and the file is git-ignored on
// purpose (see .gitignore and AGENTS.md's Firebase Analytics section). A clean checkout, a fork,
// and a fork-originated PR - which cannot read Action secrets at all - must all still compile.
// Without the plugin no `google_app_id` string resource is generated, FirebaseApp never
// initializes, and core:data's FirebaseAnalyticsTracker no-ops at runtime (see its kdoc), so the
// app behaves exactly as it did before analytics existed. The plugin itself reaches the build
// classpath via the root build.gradle.kts's `apply false` declaration.
if (layout.projectDirectory
        .file("google-services.json")
        .asFile
        .exists()
) {
    apply(plugin = "com.google.gms.google-services")
}

android {
    namespace = "org.neteinstein.family"
    compileSdk = 37

    defaultConfig {
        applicationId = "org.neteinstein.family"
        minSdk = 32
        targetSdk = 37
        // Overridable by .github/workflows/release.yml (APP_VERSION_CODE/APP_VERSION_NAME env
        // vars, derived from the GitHub Actions run number) so a release build gets a unique,
        // monotonically increasing versionCode without editing this file on every release. Local/
        // debug builds fall back to these defaults.
        versionCode = System.getenv("APP_VERSION_CODE")?.toIntOrNull() ?: 1
        versionName = System.getenv("APP_VERSION_NAME") ?: "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    // Two distribution channels built from the same codebase: "github" is the direct-APK build
    // distributed via GitHub Releases and keeps the in-app self-update check/install flow (see
    // app/src/github/AndroidManifest.xml for its extra permission/provider/receiver); "playstore"
    // is submitted to the Play Store, which reviews/distributes updates itself, so
    // UPDATES_ENABLED gates that flow off entirely (feature/settings hides the "Updates" section
    // and never invokes the update use cases - see di/AppModule.kt).
    // DISTRIBUTION is reported to Firebase Analytics as a user property (see core:domain's
    // AnalyticsUserProperties and app's appModule), so a metric can be split by install channel -
    // update adoption, for instance, only means anything for the "github" flavor.
    flavorDimensions += "distribution"
    productFlavors {
        create("github") {
            dimension = "distribution"
            buildConfigField("boolean", "UPDATES_ENABLED", "true")
            buildConfigField("String", "DISTRIBUTION", "\"github\"")
        }
        create("playstore") {
            dimension = "distribution"
            buildConfigField("boolean", "UPDATES_ENABLED", "false")
            buildConfigField("String", "DISTRIBUTION", "\"playstore\"")
        }
    }

    // The Play Publisher plugin (see the `play { }` block below) is disabled by default there and
    // only re-enabled for the "playstore" flavor here, since "github" shares the same
    // applicationId and must never be uploaded to the Play Console.
    playConfigs {
        register("playstore") {
            enabled.set(true)
        }
    }

    // Populated by .github/workflows/release.yml from Action secrets (KEYSTORE_BASE64 decoded to
    // a file + KEYSTORE_PASSWORD/KEY_ALIAS/KEY_PASSWORD) so the release workflow can produce a
    // signed APK. Left unset for local builds - see the release buildType below for the fallback.
    signingConfigs {
        create("release") {
            val keystoreFile = System.getenv("KEYSTORE_FILE")
            if (keystoreFile != null) {
                storeFile = file(keystoreFile)
                storePassword = System.getenv("KEYSTORE_PASSWORD")
                keyAlias = System.getenv("KEY_ALIAS")
                keyPassword = System.getenv("KEY_PASSWORD")
            }
        }
    }

    buildTypes {
        debug {
            isDebuggable = true
            // The only module left with this classic android-application/-library DSL now that
            // every other module (core:*, feature:*, app) has converted to a KMP android-library,
            // which has no equivalent yet (see AGENTS.md's KMP migration note) - without this,
            // CI's Code Coverage job's createDebugUnitTestCoverageReport task doesn't exist
            // anywhere in the build at all. androidApp's own MainActivityViewModelTest.kt (see its
            // comment) exists specifically to give this task real data to report on, now that
            // androidApp itself is otherwise just a thin Android application shell (Phase 7);
            // real per-module coverage for the KMP modules is a Phase 8 follow-up.
            enableUnitTestCoverage = true
        }
        release {
            // R8 runs in full mode (android.enableR8.fullMode in gradle.properties) over both
            // distribution flavors: it strips unreachable code, obfuscates what is left, and -
            // with isShrinkResources - drops resources nothing references any more. Every keep
            // rule the app actually needs (Room's reflective "_Impl" lookup, stack-trace
            // attributes) lives in proguard-rules.pro; "proguard-android-optimize.txt" is AGP's
            // own baseline (the optimizing variant, i.e. without -dontoptimize).
            isMinifyEnabled = true
            // Safe to pair with minification here because nothing in this app resolves a resource
            // dynamically (no Resources.getIdentifier) - every reference is a static R.* one that
            // the shrinker can see. Kept in lockstep with isMinifyEnabled: resource shrinking
            // relies on the code shrinker's reachability analysis and is a no-op without it.
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
            // Sign with the real release key when CI provides one; otherwise fall back to debug
            // signing so `./gradlew assembleRelease` still works on a developer machine without
            // the signing secrets configured.
            signingConfig =
                if (System.getenv("KEYSTORE_FILE") != null) {
                    signingConfigs.getByName("release")
                } else {
                    signingConfigs.getByName("debug")
                }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

// Uploads the "playstore" flavor's release App Bundle to the Play Console via the Google Play
// Developer API (invoked as `publishPlaystoreReleaseBundle` by .github/workflows/release.yml).
// Disabled by default and only turned on for the "playstore" flavor above (`playConfigs`).
// Authenticates via the ANDROID_PUBLISHER_CREDENTIALS env var (GPP's default lookup - the raw
// contents of a Play Console service account JSON key, not a file path), left unset for local
// builds where no publish task is ever invoked. Publishes to the "internal" track unless
// PLAY_TRACK overrides it, so a release never reaches production without an explicit promotion
// in the Play Console.
play {
    enabled.set(false)
    track.set(System.getenv("PLAY_TRACK")?.takeIf { it.isNotBlank() } ?: "internal")
    defaultToAppBundles.set(true)
}

ktlint {
    android.set(true)
    ignoreFailures.set(false)
}

dependencies {
    // Provides the shared App()/MainActivityViewModel/navigation/DI - MainActivity/FamilyMomentsApp
    // call straight into it (see AGENTS.md's KMP migration section). androidApp's own main code
    // has no direct dependency on any core:*/feature:* module any more (Phase 7) - everything it
    // needs from them reaches it transitively through `app`.
    implementation(project(":app"))

    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.graphics)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.compose.material3)
    implementation(libs.compose.animation)
    implementation(libs.activity.compose)
    implementation(libs.lifecycle.runtime.ktx)
    implementation(libs.navigation.compose)
    implementation(libs.splashscreen)
    implementation(libs.koin.android)
    implementation(libs.koin.androidx.compose)
    implementation(libs.core.ktx)
    implementation(libs.coroutines.android)

    debugImplementation(libs.compose.ui.tooling)
    debugImplementation(libs.compose.ui.test.manifest)

    // Only MainActivityViewModelTest.kt needs this (ThemeMode/GetThemeModeUseCase) - see its
    // comment for why the test itself still lives here rather than solely in `app`.
    testImplementation(project(":core:domain"))
    testImplementation(libs.junit)
    testImplementation(libs.mockk)
    testImplementation(libs.coroutines.test)
    androidTestImplementation(libs.junit.ext)
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(platform(libs.compose.bom))
    androidTestImplementation(libs.compose.ui.test.junit4)
}
