plugins {
    `kubernetes-conventions`
}

dependencies {
    ksp("io.micronaut.data:micronaut-data-processor")

    implementation(project(":model-contracts"))
    implementation(project(":common-service"))

    implementation("org.dmfs:oauth2-essentials:0.22.0")
    implementation("org.dmfs:httpurlconnection-executor:1.21.3")

    api("io.phasetwo:phasetwo-admin-client:0.1.8") {
        exclude(group = "com.fasterxml.jackson.module", module = "jackson-module-jaxb-annotations")
    }

    testImplementation("org.testcontainers:testcontainers")
    testImplementation("org.testcontainers:junit-jupiter")
    testImplementation("org.testcontainers:postgresql")
}
