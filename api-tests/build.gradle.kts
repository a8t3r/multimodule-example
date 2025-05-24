plugins {
    `kubernetes-conventions`
    io.micronaut.`test-resources`
}

dependencies {
    ksp(libs.ksp.jimmer)
    ksp(mn.micronaut.data.processor)

    implementation(projects.graphqlGateway)
    implementation(projects.libraryService)
    implementation(projects.regionsService)
    implementation(projects.organizationManagement)

    implementation(ktor.ktorClientCio)
    implementation(ktor.ktorSerializationJackson)
    implementation(ktor.ktorClientContentNegotiation)

    testImplementation(mn.graphql.java)
    testImplementation(mn.micronaut.data.tx)
    testImplementation(mn.micronaut.security)
    testImplementation(libs.jimmer.sql.kotlin)
}
