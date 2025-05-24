import dev.aga.gradle.versioncatalogs.Generator.generate
import dev.aga.gradle.versioncatalogs.GeneratorConfig

plugins {
    id("com.gradle.enterprise") version "3.10.3"
    id("io.micronaut.platform.catalog") version "4.4.4"
    id("dev.aga.gradle.version-catalog-generator") version "3.2.1"
}

dependencyResolutionManagement {
    repositories {
        mavenCentral()
    }

    versionCatalogs {
        val noPrefix: GeneratorConfig.UsingConfig.() -> Unit = {
            aliasPrefixGenerator = GeneratorConfig.NO_PREFIX
        }

        generate("ktor") {
            fromToml("ktor-bom", uc = noPrefix)
        }
        generate("ktn") {
            fromToml("kotlin-bom", uc = noPrefix)
        }
        generate("junit") {
            fromToml("junit-bom", uc = noPrefix)
        }
        generate("ktx") {
            fromToml("kotlinx-serialization-bom", "kotlinx-coroutines-bom", uc = noPrefix)
        }
        generate("otel") {
            fromToml("opentelemetry-bom", uc = noPrefix)
        }
    }
}

rootProject.name = "multimodule-example"
include(":graphql-gateway")
include(":model-contracts")
include(":common-service")
include(":library-service")
include(":organization-management")
include(":regions-service")
include(":api-tests")
