@file:Suppress("UnstableApiUsage")

import io.gitlab.arturbosch.detekt.Detekt
import io.gitlab.arturbosch.detekt.DetektCreateBaselineTask
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion
import org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask
import java.net.URI

plugins {
    org.jetbrains.kotlin.jvm
    alias(libs.plugins.detekt)
    alias(libs.plugins.dagCommand)
    alias(libs.plugins.dependencyAnalysis)
}

repositories {
    mavenLocal()
    mavenCentral()
    maven {
        url = URI("https://jitpack.io")
    }
}

val jdkVersion = libs.versions.jdk.get()
val kotlinVersion = libs.versions.kotlin.get().substringBeforeLast(".").replace(".", "_")

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
        freeCompilerArgs = listOf(
            "-Xjsr305=strict",
            "-Xcontext-receivers"
        )
        allWarningsAsErrors = false
        jvmTarget.set(JvmTarget.valueOf("JVM_$jdkVersion"))

        // https://github.com/google/ksp/issues/1942
        // apiVersion.set(KotlinVersion.valueOf("KOTLIN_$kotlinVersion"))
        languageVersion.set(KotlinVersion.valueOf("KOTLIN_$kotlinVersion"))
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
    implementation(ktn.kotlinStdlib)
    implementation(ktn.kotlinStdlibJdk8)
    implementation(ktx.kotlinxCoroutinesCore)
    implementation(ktx.kotlinxCoroutinesJdk8)
    implementation(ktx.kotlinxCoroutinesReactive)

    implementation(libs.commons.lang)
    implementation(libs.kotlin.logging.jvm)
    detektPlugins(libs.detekt.formatting)
    testImplementation(ktx.kotlinxCoroutinesTest)
}

tasks.withType<KotlinCompilationTask<*>>().configureEach {
    compilerOptions {
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
