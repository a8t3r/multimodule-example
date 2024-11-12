plugins {
    `kubernetes-conventions`
}

dependencies {
    ksp(libs.ksp.jimmer)
    ksp(mn.micronaut.data.processor)

    implementation(libs.jimmer.sql.kotlin)
    implementation(mn.micronaut.data.model)
}
