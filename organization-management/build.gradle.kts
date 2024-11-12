plugins {
    `kubernetes-conventions`
}

dependencies {
    ksp(libs.ksp.jimmer)
    ksp(mn.micronaut.data.processor)

    implementation(libs.phaseTwo.admin.client) {
        exclude(group = "com.fasterxml.jackson.module", module = "jackson-module-jaxb-annotations")
    }

    implementation(mn.micronaut.kafka)
    implementation(libs.jimmer.sql.kotlin)
    implementation(mn.micronaut.data.jdbc)
    implementation(mn.micronaut.data.model)
    implementation(libs.kotlin.valiktor.core)
}
