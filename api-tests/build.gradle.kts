plugins {
    `kubernetes-conventions`
    id("io.micronaut.test-resources")
}

dependencies {
    implementation(libs.graphql.scalars)

    implementation(project(":graphql-gateway"))
    implementation(project(":library-service"))
    implementation(project(":regions-service"))
    implementation(project(":organization-management"))

    testImplementation("io.micronaut.security:micronaut-security")
}
