plugins {
    id("com.gradle.enterprise") version "3.10.3"
    id("io.micronaut.platform.catalog") version "4.4.4"
}

rootProject.name = "multimodule-example"
include(":graphql-gateway")
include(":model-contracts")
include(":common-service")
include(":library-service")
include(":organization-management")
include(":regions-service")
include(":api-tests")
