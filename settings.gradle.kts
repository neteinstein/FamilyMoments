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
    // download the Node.js distribution, which FAIL_ON_PROJECT_REPOS rejects outright ("Could not
    // determine the dependencies of task ':kotlinWasmNodeJsSetup' ... repository ... was added by
    // unknown code"). PREFER_SETTINGS still prefers the repositories declared here for anything
    // they can resolve, while allowing a plugin-added repository like this one through instead of
    // hard-failing the build.
    repositoriesMode.set(RepositoriesMode.PREFER_SETTINGS)
    repositories {
        google()
        mavenCentral()
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
