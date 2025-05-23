@file:Suppress("UnstableApiUsage")

import gradle.kotlin.dsl.accessors._d31b883013e292cbd0ac2e670d6307c5.implementation
import io.gitlab.arturbosch.detekt.Detekt
import io.gitlab.arturbosch.detekt.DetektCreateBaselineTask
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinCompile
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion
import java.net.URI

plugins {
    kotlin("jvm")
    id("io.gitlab.arturbosch.detekt")
    id("com.autonomousapps.dependency-analysis")
    id("io.github.leandroborgesferreira.dag-command")
}

repositories {
    mavenLocal()
    mavenCentral()
    maven {
        url = URI("https://jitpack.io")
    }
}

val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")
val jdkVersion = libs.findVersion("jdk").get().toString()
val kotlinVersion = libs.findVersion("kotlin").get().toString().substringBeforeLast(".").replace(".", "_")

java {
    val javaVersion = JavaVersion.valueOf("VERSION_$jdkVersion")
    sourceCompatibility = javaVersion
    targetCompatibility = javaVersion
}

kotlin {
    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(jdkVersion))
    }
    compilerOptions {
        @Suppress("SpellCheckingInspection")
        freeCompilerArgs = listOf("-Xjsr305=strict", "-Xcontext-receivers")
        allWarningsAsErrors = false
        jvmTarget.set(JvmTarget.valueOf("JVM_$jdkVersion"))
        languageVersion.set(KotlinVersion.valueOf("KOTLIN_$kotlinVersion"))
        apiVersion.set(KotlinVersion.valueOf("KOTLIN_$kotlinVersion"))
    }
}

detekt {
    ignoreFailures = false
    buildUponDefaultConfig = true
    config.setFrom("$rootDir/detekt.yml")
    parallel = true
}

dagCommand {
    defaultBranch = "origin/develop"
    outputType = "json"
}

dependencies {
    implementation(platform("org.jetbrains.kotlin:kotlin-bom"))
    implementation(platform(libs.findLibrary("kotlinx-coroutines-bom").get()))

    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactive")

    implementation(libs.findLibrary("commons-lang").get())
    implementation(libs.findLibrary("kotlin-logging-jvm").get())

    detektPlugins(libs.findLibrary("detekt-formatting").get())
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test")
}

tasks.withType<KotlinCompile<*>>().configureEach {
    kotlinOptions {
        allWarningsAsErrors = false
    }
}

// Activate Type Resolution
tasks.withType<Detekt>().configureEach {
    this.jvmTarget = jdkVersion
    classpath.setFrom(
        sourceSets.main.get().compileClasspath,
        sourceSets.test.get().compileClasspath
    )
}

// Activate Type Resolution
tasks.withType<DetektCreateBaselineTask>().configureEach {
    this.jvmTarget = jdkVersion
    classpath.setFrom(
        sourceSets.main.get().compileClasspath,
        sourceSets.test.get().compileClasspath
    )
}

afterEvaluate {
    // Workaround for https://detekt.dev/docs/gettingstarted/gradle/#gradle-runtime-dependencies
    // and https://github.com/detekt/detekt/issues/6428#issuecomment-1779291878
    configurations.matching { it.name == "detekt" }.all {
        resolutionStrategy.eachDependency {
            if (requested.group == "org.jetbrains.kotlin") {
                useVersion(io.gitlab.arturbosch.detekt.getSupportedKotlinVersion())
            }
        }
    }
}
