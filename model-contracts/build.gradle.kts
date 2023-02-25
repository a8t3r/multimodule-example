plugins {
    id("com.google.devtools.ksp")
    id("kotlin-conventions")
    id("testing-conventions")
    kotlin("plugin.serialization")
}

dependencies {
    ksp(libs.micronaut.inject.kotlin)
    ksp(libs.ksp.auto.service)

    implementation(libs.auto.service)
    compileOnly(libs.kotlin.micronaut.core)
    compileOnly(libs.kotlin.graphql.schema.generator)

    api(libs.kotlinx.serialization.json)
    api(libs.kotlinx.serialization.protobuf)
}
