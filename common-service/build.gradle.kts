import com.bmuschko.gradle.docker.tasks.image.DockerPushImage

plugins {
    kotlin("plugin.serialization")
    id("micronaut-conventions")
    id("testing-conventions")
}

dependencies {
    ksp(libs.ksp.jimmer)
    annotationProcessor(mn.micronaut.tracing.opentelemetry.annotation)

    implementation(project(":model-contracts"))

    implementation(mn.kotlin.reflect)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.serialization.protobuf)
    implementation(libs.kotlin.rsocket.transport.ktor.tcp)

    implementation(mn.micronaut.redis.lettuce)
    implementation(mn.micronaut.security.jwt)
    implementation(mn.micronaut.tracing.opentelemetry.http)
    implementation(mn.micronaut.tracing.opentelemetry.kafka)
    implementation(mn.micronaut.kubernetes.discovery.client)

    implementation("io.opentelemetry:opentelemetry-exporter-otlp")

    implementation(mn.micronaut.kafka)
    implementation(mn.micronaut.data.model)
    implementation(mn.micronaut.data.jdbc)

    implementation(mn.micronaut.reactor)
    implementation(mn.jackson.databind)
    implementation(mn.jackson.module.kotlin)

    implementation(mn.guava)
    implementation(mn.logback.classic)
    implementation(libs.logitech.jts.core)
    implementation(libs.jimmer.sql.kotlin)
    implementation(libs.kotlin.logging.jvm)
    implementation(libs.kotlin.valiktor.core)

    implementation(mn.postgresql)
    implementation(mn.micronaut.flyway)
    implementation(mn.flyway.postgresql)
    implementation(mn.micronaut.jdbc.hikari)
    implementation(mn.spring.jdbc)
}

tasks.withType<DockerPushImage> {
    enabled = false
}
