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

dependencies {
    testImplementation(libs.assertk)
    testImplementation(ktn.kotlinTest)

    testImplementation(junit.junitJupiterApi)
    testImplementation(junit.junitJupiterEngine)
}
