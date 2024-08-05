import com.bmuschko.gradle.docker.tasks.image.DockerPushImage

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

    implementation("io.micronaut.redis:micronaut-redis-lettuce")
    implementation("io.micronaut.security:micronaut-security-jwt")
    implementation("io.opentelemetry:opentelemetry-exporter-otlp")
    implementation("io.micronaut.tracing:micronaut-tracing-opentelemetry-http")
    implementation("io.micronaut.tracing:micronaut-tracing-opentelemetry-kafka")
    implementation("io.micronaut.kubernetes:micronaut-kubernetes-discovery-client")

    implementation("io.micronaut.reactor:micronaut-reactor")
    implementation("com.fasterxml.jackson.core:jackson-databind")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")

    implementation(libs.guava)
    implementation(libs.kotlin.valiktor.core)
    implementation(libs.jimmer.sql.kotlin)
    implementation(libs.kotlin.logging.jvm)

    implementation("org.postgresql:postgresql")
    implementation("io.micronaut.flyway:micronaut-flyway")
    implementation("org.flywaydb:flyway-database-postgresql")
    implementation("io.micronaut.sql:micronaut-jdbc-hikari")
    implementation("org.springframework:spring-jdbc:6.1.6")
}

tasks.withType<DockerPushImage> {
    enabled = false
}
