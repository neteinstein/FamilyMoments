plugins {
    `kotlin-dsl`
}

// Precompiled script plugins (src/main/kotlin/familymoments.*.gradle.kts) apply these plugins by
// id at runtime, so they need to be on build-logic's own runtime classpath - not just compileOnly,
// which would only satisfy the `import`s their .gradle.kts files use for typed DSL access (e.g.
// `kotlin { ... }`, `ktlint { ... }`). `libs.plugins.foo` isn't itself valid dependency notation
// (verified against a real Gradle resolution failure, not assumed) - it has to be turned into the
// plugin marker artifact's coordinates first.
fun DependencyHandlerScope.pluginMarker(plugin: Provider<PluginDependency>) =
    implementation(plugin.map { "${it.pluginId}:${it.pluginId}.gradle.plugin:${it.version}" })

dependencies {
    pluginMarker(libs.plugins.kotlin.multiplatform)
    pluginMarker(libs.plugins.android.kotlin.multiplatform.library)
    pluginMarker(libs.plugins.compose.multiplatform)
    pluginMarker(libs.plugins.kotlin.compose)
    pluginMarker(libs.plugins.ktlint)
    pluginMarker(libs.plugins.kover)
}
