plugins {
    `kubernetes-conventions`
}

dependencies {
    ksp("io.micronaut.data:micronaut-data-processor")

    testImplementation("org.testcontainers:testcontainers")
    testImplementation("org.testcontainers:junit-jupiter")
    testImplementation("org.testcontainers:postgresql")
}
