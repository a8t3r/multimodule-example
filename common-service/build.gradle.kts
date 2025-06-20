import com.bmuschko.gradle.docker.tasks.image.DockerPushImage
import org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask

plugins {
    `micronaut-conventions`
    `testing-conventions`
    `kotlinx-serialization`
}

dependencies {
    ksp(libs.ksp.jimmer)
    annotationProcessor(mn.micronaut.tracing.opentelemetry.annotation)

    implementation(projects.modelContracts)

    implementation(mn.kotlin.reflect)
    implementation(ktx.kotlinxSerializationJson)
    implementation(ktx.kotlinxSerializationProtobuf)
    implementation(libs.kotlin.rsocket.transport.ktor.tcp)

    implementation(mn.micronaut.jmx)
    implementation(mn.micronaut.security.jwt)
    implementation(mn.micronaut.redis.lettuce)
    implementation(mn.micronaut.tracing.opentelemetry.http)
    implementation(mn.micronaut.tracing.opentelemetry.kafka)
    implementation(mn.micronaut.kubernetes.discovery.client)

    implementation(libs.jasync.postgresql)
    implementation(otel.opentelemetryExporterOtlp)

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

tasks.withType<KotlinCompilationTask<*>>().configureEach {
    compilerOptions {
        optIn.add("kotlinx.serialization.ExperimentalSerializationApi")
    }
}
