@file:Suppress("UnstableApiUsage")

import gradle.kotlin.dsl.accessors._39e098789f9a3862479dce1fe3e5f9d3.implementation
import gradle.kotlin.dsl.accessors._39e098789f9a3862479dce1fe3e5f9d3.testImplementation
import io.gitlab.arturbosch.detekt.Detekt
import org.jetbrains.kotlin.gradle.plugin.getKotlinPluginVersion
import java.net.URI

plugins {
    id("kotlin-conventions")

    // Apply the Kotlin JVM plugin to add support for Kotlin on the JVM.
    kotlin("jvm")

    // A tool to detect kotlin problems. It's nice, give it a try!
    id("io.gitlab.arturbosch.detekt")
}

repositories {
    mavenCentral()
    maven {
        url = URI("https://jitpack.io")
    }
}

val embeddedMajorAndMinorKotlinVersion = project.getKotlinPluginVersion().substringBeforeLast(".")
if (KOTLIN_VERSION != embeddedMajorAndMinorKotlinVersion) {
    logger.warn("Constant 'KOTLIN_VERSION' ($KOTLIN_VERSION) differs from embedded Kotlin version in Gradle (${project.getKotlinPluginVersion()})!\n" +
            "Constant 'KOTLIN_VERSION' should be ($embeddedMajorAndMinorKotlinVersion).")
}

tasks.compileKotlin {
    logger.lifecycle("Configuring $name with version ${project.getKotlinPluginVersion()} in project ${project.name}")
    kotlinOptions {
        @Suppress("SpellCheckingInspection")
        freeCompilerArgs = listOf("-Xjsr305=strict")
        allWarningsAsErrors = true
        jvmTarget = JDK_VERSION
        languageVersion = KOTLIN_VERSION
        apiVersion = KOTLIN_VERSION
    }
}

tasks.compileTestKotlin {
    logger.lifecycle("Configuring $name with version ${project.getKotlinPluginVersion()} in project ${project.name}")
    kotlinOptions {
        @Suppress("SpellCheckingInspection")
        freeCompilerArgs = listOf("-Xjsr305=strict")
        allWarningsAsErrors = true
        jvmTarget = JDK_VERSION
        languageVersion = KOTLIN_VERSION
        apiVersion = KOTLIN_VERSION
    }
}

kotlin {
    jvmToolchain {
        languageVersion.set(
            JavaLanguageVersion.of(JDK_VERSION)
        )
    }
}

detekt {
    ignoreFailures = false
    buildUponDefaultConfig = true
    config = files("$rootDir/detekt.yml")
    parallel = true
}

val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")

dependencies {
    constraints {
        implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    }

    implementation(platform("org.jetbrains.kotlin:kotlin-bom"))

    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

    val logbackVersion = libs.findVersion("logback").get()
    implementation("ch.qos.logback:logback-classic:$logbackVersion")

    val coroutinesVersion = libs.findVersion("coroutines").get()
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutinesVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:$coroutinesVersion")

    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:$coroutinesVersion")

    add("detektPlugins", libs.findLibrary("detekt-formatting").get())
}

tasks.withType<Detekt>().configureEach {
    // Target version of the generated JVM bytecode. It is used for type resolution.
    this.jvmTarget = JDK_VERSION

    onlyIf { project.hasProperty("runDetekt") }
}
