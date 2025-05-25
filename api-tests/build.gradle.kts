import io.github.ermadmi78.kobby.kobby
import io.github.ermadmi78.kobby.task.KobbyKotlin
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    `kubernetes-conventions`
    io.micronaut.`test-resources`
    alias(libs.plugins.kobby)
}

kobby {
    kotlin {
        dto {
            serialization {
                enabled = false
            }
        }
        adapter {
            extendedApi = true
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

    implementation(projects.graphqlGateway)
    implementation(projects.libraryService)
    implementation(projects.regionsService)
    implementation(projects.organizationManagement)

    implementation(ktor.ktorClientCio)
    implementation(ktor.ktorSerializationJackson)
    implementation(ktor.ktorClientContentNegotiation)

    testImplementation(mn.graphql.java)
    testImplementation(mn.micronaut.data.tx)
    testImplementation(mn.micronaut.security)
    testImplementation(libs.jimmer.sql.kotlin)
}

tasks.withType(KotlinCompile::class.java).configureEach {
    dependsOn(tasks.withType(KobbyKotlin::class.java))
}