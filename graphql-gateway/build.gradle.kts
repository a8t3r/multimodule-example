plugins {
    id("kotlin-conventions")
    id("testing-conventions")
    id("dokka-conventions")
    id("micronaut-conventions")
}

dependencies {
    implementation("io.micronaut.graphql:micronaut-graphql")
    implementation(libs.kotlin.graphql)
    implementation(libs.graphql.scalars)

    implementation(project(":library-service"))
    implementation(project(":model-contracts"))
}
