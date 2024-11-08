@file:Suppress("UnstableApiUsage")

import io.gitlab.arturbosch.detekt.Detekt
import io.gitlab.arturbosch.detekt.DetektCreateBaselineTask
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinCompile
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion
import java.net.URI

plugins {
    kotlin("jvm")
    id("io.gitlab.arturbosch.detekt")
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
    constraints {
        implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    }

    implementation(platform("org.jetbrains.kotlin:kotlin-bom"))
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

    implementation(libs.findLibrary("logback").get())
    implementation(libs.findLibrary("commons-lang").get())
    implementation(libs.findLibrary("kotlinx-coroutines-core").get())
    implementation(libs.findLibrary("kotlinx-coroutines-jdk8").get())
    implementation(libs.findLibrary("kotlinx-coroutines-reactive").get())

    testImplementation(libs.findLibrary("kotlinx-coroutines-test").get())
    testImplementation(libs.findLibrary("truth").get())

    detektPlugins(libs.findLibrary("detekt-formatting").get())
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
