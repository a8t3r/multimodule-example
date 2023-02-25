plugins {
    id("kotlin-conventions")
    id("io.micronaut.application")
    id("io.micronaut.docker")
    id("com.google.devtools.ksp")
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

val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")
val micronautVersion = libs.findVersion("micronaut").get().toString()

micronaut {
    version.set(micronautVersion)
    runtime("netty")
    testRuntime("junit5")
}

dependencies {
    implementation("io.micronaut:micronaut-http-server-netty")
    implementation("io.micronaut.kotlin:micronaut-kotlin-runtime")
    implementation("io.micronaut.kotlin:micronaut-kotlin-extension-functions")

    implementation("io.micronaut:micronaut-management")
    implementation("io.micronaut.micrometer:micronaut-micrometer-core")
    implementation("io.micronaut.micrometer:micronaut-micrometer-registry-prometheus")

    implementation("io.micronaut:micronaut-http-client")
    implementation("io.micronaut.kotlin:micronaut-kotlin-runtime")

    runtimeOnly("org.yaml:snakeyaml")
    testImplementation("io.micronaut.testresources:micronaut-test-resources-jdbc-postgresql")
}
