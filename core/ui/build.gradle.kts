import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.kotlin.multiplatform.library)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ktlint)
}

// Shared Compose Multiplatform theme (Color/Theme/Typography) + the campfire artwork - see
// AGENTS.md's KMP migration section. Exposes Compose libs via `api` so feature modules keep
// getting them transitively without declaring their own dependency, same as before this
// conversion.
kotlin {
    jvmToolchain(17)

    @OptIn(ExperimentalKotlinGradlePluginApi::class)
    android {
        namespace = "org.neteinstein.family.ui"
        compileSdk = 37
        minSdk = 32
    }

    iosArm64()
    iosSimulatorArm64()

    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser()
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
            api(libs.lifecycle.runtime.compose)
            api(libs.lifecycle.viewmodel.compose)
        }
    }
}

compose.resources {
    packageOfResClass = "org.neteinstein.family.ui.resources"
    // Generated resource accessors (Res.drawable.*) default to module-internal visibility -
    // feature:splash (a different module) needs to reach them for the campfire artwork.
    publicResClass = true
}

ktlint {
    // The Compose resource generator (compose.components.resources above) emits Kotlin under
    // build/generated/compose/resourceGenerator/... that doesn't follow this project's style -
    // exclude anything under a "generated" path rather than lint it (see AGENTS.md's KMP
    // migration section for the CI failure this avoids).
    filter {
        exclude { entry -> entry.file.path.contains("${File.separatorChar}generated${File.separatorChar}") }
    }
    reporters {
        reporter(org.jlleitschuh.gradle.ktlint.reporter.ReporterType.PLAIN)
        reporter(org.jlleitschuh.gradle.ktlint.reporter.ReporterType.CHECKSTYLE)
    }
}
