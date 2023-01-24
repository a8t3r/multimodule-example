plugins {
    id("com.gradle.enterprise") version ("3.10.3")
}

rootProject.name = "multimodule-example"
include(":graphql-gateway")
include(":model-contracts")
include(":library-service")
include(":identity-management")

