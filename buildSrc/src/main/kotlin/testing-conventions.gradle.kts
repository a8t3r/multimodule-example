import org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
import org.gradle.api.tasks.testing.logging.TestLogEvent.FAILED

plugins {
    id("kotlin-conventions")
}

tasks.test {
    useJUnitPlatform()
    testLogging {
        events = setOf(FAILED)
        exceptionFormat = FULL
    }
}

val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")

dependencies {
    testImplementation(libs.findLibrary("kotlinx-coroutines-test").get())
    testImplementation(libs.findLibrary("truth").get())

    val junit = libs.findVersion("junit").get().toString()
    testImplementation("org.junit.jupiter:junit-jupiter-api:$junit")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:$junit")
}
