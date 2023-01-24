plugins {
    id("kotlin-conventions")
    id("testing-conventions")
    id("dokka-conventions")
    kotlin("plugin.serialization")
}

dependencies {
    implementation(libs.kotlin.micronaut.core)
    implementation(libs.kotlin.micronaut.security.annotations)
    implementation(libs.kotlinx.serialization.json)
    compileOnly(libs.kotlin.graphql.schema.generator)
}
