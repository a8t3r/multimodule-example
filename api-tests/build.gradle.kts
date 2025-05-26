import io.github.ermadmi78.kobby.kobby
import io.github.ermadmi78.kobby.task.KobbyKotlin
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    `kotlinx-serialization`
    `kubernetes-conventions`
    io.micronaut.`test-resources`
    alias(libs.plugins.kobby)
}

kobby {
    kotlin {
        dto {
            serialization {
                enabled = true
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
            "UUID" to typeOf("java.util", "UUID")
                .serializer("io.eordie.multimodule.contracts.utils.JsonModule", "UUIDSerializer"),
            "Date" to typeOf("java.time", "LocalDate"),
            "DateTime" to typeOf("java.time", "OffsetDateTime")
                .serializer("io.eordie.multimodule.contracts.utils.JsonModule", "OffsetDateTimeSerializer"),
            "TPoint" to typeOf("io.eordie.multimodule.contracts.basic.geometry", "GeoJsonPoint"),
            "TPolygon" to typeOf("io.eordie.multimodule.contracts.basic.geometry", "GeoJsonPolygon"),
            "TMultiPolygon" to typeOf("io.eordie.multimodule.contracts.basic.geometry", "GeoJsonMultiPolygon")
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
    implementation(ktx.kotlinxSerializationJson)
    implementation(ktor.ktorSerializationKotlinxJson)
    implementation(ktor.ktorClientContentNegotiationJvm)

    testImplementation(mn.graphql.java)
    testImplementation(mn.micronaut.data.tx)
    testImplementation(mn.micronaut.security)
    testImplementation(libs.jimmer.sql.kotlin)
}

tasks.withType(KotlinCompile::class.java).configureEach {
    dependsOn(tasks.withType(KobbyKotlin::class.java))
}