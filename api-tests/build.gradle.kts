import io.github.ermadmi78.kobby.kobby
import io.github.ermadmi78.kobby.task.KobbyKotlin
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    `kubernetes-conventions`
    id("io.micronaut.test-resources")
    id("io.github.ermadmi78.kobby")
}

kobby {
    kotlin {
        adapter {
            ktor {
                simpleEnabled = true
                compositeEnabled = false
            }
        }
        scalars = mapOf(
            "UUID" to typeOf("java.util", "UUID"),
            "Date" to typeOf("java.time", "LocalDate"),
            "DateTime" to typeOf("java.time", "OffsetDateTime"),
            "TPoint" to typeOf("io.eordie.multimodule.contracts.basic.geometry", "TPoint"),
            "TPolygon" to typeOf("io.eordie.multimodule.contracts.basic.geometry", "TPolygon"),
            "TMultiPolygon" to typeOf("io.eordie.multimodule.contracts.basic.geometry", "TMultiPolygon")
        )
    }
}

dependencies {
    ksp(libs.ksp.jimmer)
    ksp(mn.micronaut.data.processor)

    implementation(project(":graphql-gateway"))
    implementation(project(":library-service"))
    implementation(project(":regions-service"))
    implementation(project(":organization-management"))

    implementation(libs.ktor.client.cio)
    implementation(libs.ktor.serialization.jackson)
    implementation(libs.ktor.client.content.negotiation)

    testImplementation(mn.graphql.java)
    testImplementation(mn.micronaut.data.tx)
    testImplementation(mn.micronaut.security)
    testImplementation(libs.jimmer.sql.kotlin)
}

tasks.withType(KotlinCompile::class.java).configureEach {
    dependsOn(tasks.withType(KobbyKotlin::class.java))
}