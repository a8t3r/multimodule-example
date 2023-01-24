plugins {
    id("kotlin-conventions")
    kotlin("kapt")
    id("io.micronaut.application")
    id("io.micronaut.docker")
}

repositories {
    mavenCentral()
}

java {
    toolchain {
        implementation.set(JvmImplementation.J9)
    }
}

graalvmNative.toolchainDetection.set(false)

val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")
val micronautVersion = libs.findVersion("micronaut").get().toString()

micronaut {
    version.set(micronautVersion)
    runtime("netty")
    testRuntime("junit5")
}

dependencies {
    kapt("io.micronaut:micronaut-http-validation")
    implementation("io.micronaut:micronaut-validation")
    implementation("io.micronaut:micronaut-http-server-netty")
    implementation("io.micronaut.kotlin:micronaut-kotlin-runtime")
    implementation("io.micronaut.kotlin:micronaut-kotlin-extension-functions")

    implementation("io.micronaut:micronaut-management")
    implementation("io.micronaut.micrometer:micronaut-micrometer-core")
    implementation("io.micronaut.micrometer:micronaut-micrometer-registry-prometheus")

    implementation("io.micronaut:micronaut-http-client")
    implementation("jakarta.annotation:jakarta.annotation-api")
}

logger.lifecycle("Enabling Micronaut application plugin in module ${project.path}")
apply(plugin = "io.micronaut.application")
