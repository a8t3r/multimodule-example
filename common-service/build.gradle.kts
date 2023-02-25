plugins {
    kotlin("plugin.serialization")
    id("micronaut-conventions")
    id("testing-conventions")
}

dependencies {
    ksp(libs.ksp.jimmer)
    annotationProcessor("io.micronaut.tracing:micronaut-tracing-opentelemetry-annotation")

    implementation(project(":model-contracts"))
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.serialization.protobuf)
    implementation(libs.kotlin.rsocket.transport.ktor.tcp)

    api("io.opentelemetry:opentelemetry-api")
    api("io.opentelemetry:opentelemetry-context")
    api("io.opentelemetry:opentelemetry-extension-kotlin")
    implementation("io.opentelemetry:opentelemetry-exporter-otlp")
    implementation("io.micronaut.tracing:micronaut-tracing-opentelemetry-http")

    implementation("io.micronaut.reactor:micronaut-reactor")
    implementation("io.micronaut.discovery:micronaut-discovery-client")

    implementation("com.fasterxml.jackson.core:jackson-databind")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")

    api(libs.guava)
    api(libs.kotlin.konform)
    api(libs.jimmer.sql.kotlin)
    api(libs.kotlin.logging.jvm)

    api("org.postgresql:postgresql")
    api("io.micronaut.flyway:micronaut-flyway")
    api("org.flywaydb:flyway-database-postgresql")
    api("org.springframework:spring-jdbc:6.1.6")
    api("io.micronaut.sql:micronaut-jdbc-hikari")
    api("io.micronaut.data:micronaut-data-model")
    api("io.micronaut.data:micronaut-data-jdbc")
}
