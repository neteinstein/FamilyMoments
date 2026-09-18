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
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
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
