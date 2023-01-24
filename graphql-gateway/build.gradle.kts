plugins {
    id("kotlin-conventions")
    id("testing-conventions")
    id("dokka-conventions")
    id("micronaut-conventions")
}

dependencies {
    implementation("io.micronaut.graphql:micronaut-graphql")
    implementation("io.micronaut.security:micronaut-security-jwt")
    implementation("io.micronaut.security:micronaut-security-oauth2")

    implementation(libs.kotlin.graphql)
    implementation(libs.graphql.scalars)

    implementation(project(":model-contracts"))
    implementation(project(":library-service"))
    implementation(project(":identity-management"))
}
