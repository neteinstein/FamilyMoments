import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.kotlin.compose)
    id("familymoments.ktlint")
}

// The Web (wasmJs) entry point - mirrors androidApp/iosApp's role for Android/iOS: a thin
// platform-specific wrapper around `app`'s shared `App()` composable. See AGENTS.md's KMP
// migration section.
// Firebase JS SDK version pulled from the gstatic CDN by the generated firebase-init.js. Pinned
// rather than floating: an unpinned CDN URL would let a remote change alter what ships without a
// commit here.
val firebaseWebSdkVersion = "12.19.0"

// The web Firebase config never enters the repo (see AGENTS.md's Firebase Analytics section).
// Both sources are read through Gradle's `providers` API rather than System.getenv/File.readText
// so they register as proper configuration inputs - this build has org.gradle.configuration-cache
// enabled repo-wide, and a raw read would be silently cached and go stale.
val firebaseWebConfig: Provider<String> =
    providers
        .environmentVariable("FIREBASE_WEB_CONFIG")
        .orElse(providers.fileContents(layout.projectDirectory.file("firebase-web-config.json")).asText)
        .map { it.trim() }
        .orElse("")

// Renders firebase-init.js.template into a generated resource that ships next to index.html.
// With no config available - a clean checkout, or a fork's CI, which cannot read Action secrets -
// it emits a stub that leaves window.__familyMomentsAnalytics null, and core:data's
// FirebaseWebAnalyticsTracker no-ops. The web build therefore never depends on the config
// existing.
val generateFirebaseWebInit =
    tasks.register("generateFirebaseWebInit") {
        val config = firebaseWebConfig
        val template = layout.projectDirectory.file("firebase-init.js.template")
        val outputFile = layout.buildDirectory.file("generated/firebaseWeb/firebase-init.js")
        val sdkVersion = firebaseWebSdkVersion

        inputs.file(template).withPropertyName("template")
        inputs.property("config", config)
        inputs.property("sdkVersion", sdkVersion)
        outputs.file(outputFile).withPropertyName("firebaseInitJs")

        doLast {
            val rawConfig = config.get()
            val contents =
                if (rawConfig.isEmpty()) {
                    "// No Firebase web config was available at build time (FIREBASE_WEB_CONFIG unset and\n" +
                        "// webApp/firebase-web-config.json absent), so analytics is disabled for this build.\n" +
                        "window.__familyMomentsAnalytics = null;\n"
                } else {
                    template
                        .asFile
                        .readText()
                        .replace("__FIREBASE_CONFIG__", rawConfig)
                        .replace("__FIREBASE_SDK_VERSION__", sdkVersion)
                }
            outputFile.get().asFile.apply {
                parentFile.mkdirs()
                writeText(contents)
            }
        }
    }

kotlin {
    jvmToolchain(17)

    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser {
            commonWebpackConfig {
                outputFileName = "familymoments.js"
            }
        }
        binaries.executable()
    }

    sourceSets {
        // Puts the generated firebase-init.js alongside index.html in the browser distribution.
        // Wiring it as a resources srcDir (rather than copying in a doLast) is what makes the
        // processResources task depend on the generator, so a stale config can't ship.
        wasmJsMain {
            resources.srcDir(generateFirebaseWebInit.map { layout.buildDirectory.dir("generated/firebaseWeb") })
        }

        wasmJsMain.dependencies {
            implementation(projects.app)
            // `implementation(projects.app)` isn't transitive, so app's own compose deps don't
            // leak into webApp's compile classpath - ComposeViewport needs its own dependency
            // here regardless. Referenced by coordinate rather than the `compose.ui` version
            // catalog accessor, which is deprecated in this Compose Multiplatform version.
            implementation("org.jetbrains.compose.ui:ui:${libs.versions.composeMultiplatform.get()}")
        }
    }
}
