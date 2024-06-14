plugins {
    `kubernetes-conventions`
    id("io.micronaut.test-resources")
}

dependencies {
    ksp("io.micronaut.data:micronaut-data-processor")
}
