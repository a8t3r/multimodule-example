plugins {
    `kubernetes-conventions`
    io.micronaut.`test-resources`
}

dependencies {
    ksp(libs.ksp.jimmer)
    ksp(mn.micronaut.data.processor)

    implementation(project(":graphql-gateway"))
    implementation(project(":library-service"))
    implementation(project(":regions-service"))
    implementation(project(":organization-management"))

    implementation(ktor.ktorClientCio)
    implementation(ktor.ktorSerializationJackson)
    implementation(ktor.ktorClientContentNegotiation)

    testImplementation(mn.graphql.java)
    testImplementation(mn.micronaut.data.tx)
    testImplementation(mn.micronaut.security)
    testImplementation(libs.jimmer.sql.kotlin)
}
