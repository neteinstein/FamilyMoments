// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.compose.multiplatform) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.play.publisher) apply false
    // Applied for real (not `apply false`) - unlike the plugins above, this root project is
    // itself the Kover "merging module" that aggregates coverage from the KMP modules listed
    // below, via `kover(project(...))` dependencies. AGP's classic `enableUnitTestCoverage`
    // (still used by androidApp alone, see its build.gradle.kts) has no equivalent for the KMP
    // android-library plugin every core:*/feature:*/app module uses instead - see AGENTS.md's
    // KMP migration section.
    alias(libs.plugins.kover)
}

kover {
    reports {
        total {
            xml {
                onCheck = false
            }
        }
    }
}

dependencies {
    kover(project(":core:domain"))
    kover(project(":core:data"))
    kover(project(":core:ui"))
    kover(project(":feature:splash"))
    kover(project(":feature:home"))
    kover(project(":feature:settings"))
    kover(project(":app"))
}

// No manual root "clean" task here (Android Studio's usual boilerplate) - `app`'s wasmJs target
// pulls in Kotlin's Node.js/Yarn tooling plugin, which applies the `base` plugin (and its own
// "clean" task) to the root project. A manually-registered "clean" here collides with that
// ("Cannot add task 'clean' as a task with that name already exists"), so root cleaning is left
// to the task that plugin now provides.
