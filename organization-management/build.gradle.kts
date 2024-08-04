plugins {
    `kubernetes-conventions`
}

dependencies {
    ksp("io.micronaut.data:micronaut-data-processor")

    implementation("io.phasetwo:phasetwo-admin-client:0.1.9") {
        exclude(group = "com.fasterxml.jackson.module", module = "jackson-module-jaxb-annotations")
    }

    testImplementation("org.testcontainers:testcontainers")
    testImplementation("org.testcontainers:junit-jupiter")
    testImplementation("org.testcontainers:postgresql")
}
