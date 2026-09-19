pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    // PREFER_SETTINGS rather than FAIL_ON_PROJECT_REPOS: the Kotlin Gradle plugin's wasmJs/JS
    // Node.js setup (:kotlinWasmNodeJsSetup) registers its own repository (nodejs.org/dist) to
    // download the Node.js distribution, which FAIL_ON_PROJECT_REPOS rejects outright. Switching
    // to PREFER_SETTINGS alone wasn't enough - the detached configuration that plugin resolves
    // through doesn't fall back to a project-added repository under either mode - so the Node.js
    // distribution repository is declared here directly instead, matching the standard workaround
    // for this well-known Kotlin/JS+Wasm + centralized-repository-management conflict (see
    // https://youtrack.jetbrains.com/issue/KT-52626).
    repositoriesMode.set(RepositoriesMode.PREFER_SETTINGS)
    repositories {
        google()
        mavenCentral()
        ivy("https://nodejs.org/dist/") {
            name = "Node Distributions at https://nodejs.org/dist"
            patternLayout {
                artifact("v[revision]/[artifact](-v[revision]-[classifier]).[ext]")
            }
            metadataSources { artifact() }
            content { includeModule("org.nodejs", "node") }
        }
        // Same conflict, same fix, for the Yarn distribution :kotlinWasmYarnSetup downloads.
        ivy("https://github.com/yarnpkg/yarn/releases/download") {
            name = "Yarn Distributions at https://github.com/yarnpkg/yarn/releases/download"
            patternLayout {
                artifact("v[revision]/[artifact](-v[revision]).[ext]")
            }
            metadataSources { artifact() }
            content { includeModule("com.yarnpkg", "yarn") }
        }
        // Same conflict, same fix, for the Binaryen (wasm optimizer) distribution
        // :webApp:kotlinWasmBinaryenSetup downloads.
        ivy("https://github.com/WebAssembly/binaryen/releases/download") {
            name = "Binaryen Distributions at https://github.com/WebAssembly/binaryen/releases/download"
            patternLayout {
                artifact("version_[revision]/[artifact]-version_[revision]-[classifier].[ext]")
            }
            metadataSources { artifact() }
            content { includeModule("com.github.webassembly", "binaryen") }
        }
    }
}

rootProject.name = "FamilyMoments"
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

// `app` is the shared Kotlin Multiplatform aggregator module (commonMain UI/navigation/DI,
// consumed by androidApp/iosApp/webApp) - see AGENTS.md's KMP migration section. It plays the
// role loopgain's `composeApp` module plays; kept named `app` since that's this repo's existing
// convention.
include(":app")
include(":androidApp")
// `iosApp` has no build.gradle.kts - it's an Xcode project wrapper only, included so IDEs/tooling
// see it as part of the project, exactly as in loopgain.
include(":iosApp")
include(":webApp")
include(":core:domain")
include(":core:data")
include(":core:ui")
include(":feature:splash")
include(":feature:home")
include(":feature:settings")
