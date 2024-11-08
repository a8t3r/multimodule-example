plugins {
    `kubernetes-conventions`
    id("io.micronaut.test-resources")
}

dependencies {
    ksp(mn.micronaut.data.processor)
    implementation(libs.kotlin.valiktor.core)
    implementation(mn.micronaut.data.model)
    implementation(mn.micronaut.data.jdbc)
}
