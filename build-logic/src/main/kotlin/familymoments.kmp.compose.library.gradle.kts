plugins {
    id("familymoments.kmp.library")
    id("org.jetbrains.compose")
    id("org.jetbrains.kotlin.plugin.compose")
}

ktlint {
    // The Compose resource generator (each consuming module's own `compose.resources { }` block)
    // emits Kotlin under build/generated/compose/resourceGenerator/... that doesn't follow this
    // project's style - exclude anything under a "generated" path rather than lint it (see
    // AGENTS.md's KMP migration section for the CI failure this avoids).
    filter {
        exclude { entry -> entry.file.path.contains("${File.separatorChar}generated${File.separatorChar}") }
    }
}
