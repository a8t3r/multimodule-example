plugins {
    id("com.google.devtools.ksp")
    id("kotlin-conventions")
    id("testing-conventions")
    kotlin("plugin.serialization")
}

dependencies {
    ksp(mn.micronaut.inject.kotlin)
    ksp(libs.ksp.auto.service)

    implementation(libs.auto.service)
    compileOnly(mn.micronaut.core)
    compileOnly(libs.kotlin.graphql.schema.generator)

    implementation(ktx.kotlinxSerializationJson)
    implementation(ktx.kotlinxSerializationProtobuf)
}
