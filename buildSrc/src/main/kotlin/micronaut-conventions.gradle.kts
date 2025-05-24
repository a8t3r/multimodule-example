plugins {
    id("kotlin-conventions")
    alias(libs.plugins.micronaut)
    alias(libs.plugins.ksp)
}

repositories {
    mavenCentral()
}

graalvmNative.toolchainDetection.set(false)

kotlin {
    sourceSets.main {
        kotlin.srcDir("build/generated/ksp/main/kotlin")
    }
}

micronaut {
    version.set(libs.versions.micronaut.get())
    runtime("netty")
    testRuntime("junit5")
}

dependencies {
    implementation(mn.micronaut.http.server.netty)
    implementation(mn.micronaut.kotlin.runtime)
    implementation(mn.micronaut.kotlin.extension.functions)

    implementation(mn.micronaut.management)
    implementation(mn.micronaut.http.client)
    implementation(mn.micronaut.micrometer.core)
    implementation(mn.micronaut.micrometer.registry.prometheus)

    implementation(otel.opentelemetryApi)
    implementation(otel.opentelemetryContext)
    implementation(otel.opentelemetryExtensionKotlin) {
        exclude(group = "org.jetbrains.kotlin")
    }

    runtimeOnly(mn.snakeyaml)
    testImplementation("io.micronaut.testresources:micronaut-test-resources-jdbc-postgresql")
}
