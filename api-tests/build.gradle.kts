plugins {
    `kubernetes-conventions`
    id("io.micronaut.test-resources")
}

dependencies {
    implementation(libs.graphql.scalars)

    implementation(project(":model-contracts"))
    implementation(project(":common-service"))
    implementation(project(":graphql-gateway"))
    implementation(project(":library-service"))
    implementation(project(":organization-management"))

    testImplementation("io.micronaut.security:micronaut-security")
}
