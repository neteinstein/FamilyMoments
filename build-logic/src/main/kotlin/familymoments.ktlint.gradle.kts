plugins {
    id("org.jlleitschuh.gradle.ktlint")
}

// Reporter setup every module (KMP or classic android-application) wants identically - see
// AGENTS.md's KMP migration section on the `build-logic` convention plugins these factor out.
ktlint {
    reporters {
        reporter(org.jlleitschuh.gradle.ktlint.reporter.ReporterType.PLAIN)
        reporter(org.jlleitschuh.gradle.ktlint.reporter.ReporterType.CHECKSTYLE)
    }
}
