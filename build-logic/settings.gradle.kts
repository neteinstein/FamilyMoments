pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
    // Reuses the root project's own catalog rather than duplicating version numbers here - this
    // build-logic project's convention plugins (src/main/kotlin/familymoments.*.gradle.kts) apply
    // the exact same plugin versions every app module already resolves through `libs.plugins.*`.
    versionCatalogs {
        create("libs") {
            from(files("../gradle/libs.versions.toml"))
        }
    }
}

rootProject.name = "build-logic"
