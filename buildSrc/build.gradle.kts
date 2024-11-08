plugins {
    `kotlin-dsl`
}

repositories {
    gradlePluginPortal()
    mavenCentral()
}

dependencies {
    implementation(libs.ksp.gradle)
    implementation(libs.kotlin.gradle)
    implementation(libs.kotlin.allopen.gradle)
    implementation(libs.kotlin.noarg.gradle)
    implementation(libs.detekt.gradle)
    implementation(libs.dag.command.gradle)
    implementation(libs.micronaut.gradle)
    implementation(libs.kotlinx.serialization.gradle)
    implementation(libs.dependency.analysis.gradle)
}
