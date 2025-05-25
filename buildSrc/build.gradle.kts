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
    implementation(libs.plugins.kotlin.fqn("org.jetbrains.kotlin:kotlin-gradle-plugin"))
    implementation(libs.plugins.kotlinSerialization.fqn("org.jetbrains.kotlin:kotlin-serialization"))
}

fun Provider<PluginDependency>.fqn(fullName: String): String {
    return "$fullName:${get().version}"
}
