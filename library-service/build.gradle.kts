plugins {
    `kubernetes-conventions`
    id("io.micronaut.test-resources")
}

dependencies {
    ksp(libs.ksp.jimmer)
    ksp(mn.micronaut.data.processor)

    implementation(libs.jimmer.sql.kotlin)
    implementation(mn.micronaut.data.jdbc)
    implementation(mn.micronaut.data.model)
    implementation(libs.kotlin.valiktor.core)
}
