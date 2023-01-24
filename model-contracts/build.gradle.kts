plugins {
    id("kotlin-conventions")
    id("testing-conventions")
    id("dokka-conventions")
}

dependencies {
    implementation(libs.kotlin.micronaut.core)
    compileOnly(libs.kotlin.graphql.schema.generator)
}
