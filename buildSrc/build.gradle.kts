plugins {
    `kotlin-dsl`
}

repositories {
    gradlePluginPortal()
    mavenCentral()
}

dependencies {
    // Accept plugin declarations from version catalog also as libraries
    // https://github.com/gradle/gradle/issues/17963
    implementation(libs.plugins.ksp.fqn("com.google.devtools.ksp:symbol-processing-gradle-plugin"))
    implementation(libs.plugins.kotlinAllOpen.fqn("org.jetbrains.kotlin:kotlin-allopen"))
    implementation(libs.plugins.kotlinNoArg.fqn("org.jetbrains.kotlin:kotlin-noarg"))
    implementation(libs.plugins.kotlinSerialization.fqn("org.jetbrains.kotlin:kotlin-serialization"))
    implementation(libs.plugins.micronaut.fqn("io.micronaut.gradle:micronaut-gradle-plugin"))
    implementation(libs.plugins.kotlin.fqn("org.jetbrains.kotlin:kotlin-gradle-plugin"))
    implementation(libs.plugins.detekt.fqn("io.gitlab.arturbosch.detekt:detekt-gradle-plugin"))
    implementation(libs.plugins.dagCommand.fqn("io.github.leandroborgesferreira:dag-command"))
    implementation(libs.plugins.dependencyAnalysis.fqn("com.autonomousapps:dependency-analysis-gradle-plugin"))
}

fun Provider<PluginDependency>.fqn(fullName: String): String {
    return "$fullName:${get().version}"
}
