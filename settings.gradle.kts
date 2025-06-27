import dev.aga.gradle.versioncatalogs.Generator.generate
import dev.aga.gradle.versioncatalogs.GeneratorConfig

plugins {
    id("com.gradle.enterprise") version "3.19.2"
    id("io.micronaut.platform.catalog") version "4.5.4"
    id("dev.aga.gradle.version-catalog-generator") version "3.2.2"
}

dependencyResolutionManagement {
    repositories {
        mavenCentral()
    }

    versionCatalogs {
        fun String.generateFor(libraryAlias: String, vararg additionalAliases: String) {
            generate(this) {
                fromToml(libraryAlias, *additionalAliases) {
                    aliasPrefixGenerator = GeneratorConfig.NO_PREFIX
                }
            }
        }

        "ktor".generateFor("ktor-bom")
        "ktn".generateFor("kotlin-bom")
        "junit".generateFor("junit-bom")
        "otel".generateFor("opentelemetry-bom")
        "ktx".generateFor("kotlinx-serialization-bom", "kotlinx-coroutines-bom")
    }
}

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

rootProject.name = "multimodule-example"
include(":graphql-gateway")
include(":model-contracts")
include(":common-service")
include(":library-service")
include(":organization-management")
include(":regions-service")
include(":api-tests")
