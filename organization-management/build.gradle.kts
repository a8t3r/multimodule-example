plugins {
    `kubernetes-conventions`
}

dependencies {
    ksp(mn.micronaut.data.processor)

    implementation(libs.phaseTwo.admin.client) {
        exclude(group = "com.fasterxml.jackson.module", module = "jackson-module-jaxb-annotations")
    }

    implementation(libs.kotlin.valiktor.core)
    implementation(mn.micronaut.kafka)
    implementation(mn.micronaut.data.jdbc)
    implementation(mn.micronaut.data.model)
}
