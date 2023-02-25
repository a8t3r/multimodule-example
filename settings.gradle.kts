plugins {
    id("com.gradle.enterprise") version ("3.10.3")
}

rootProject.name = "multimodule-example"
include(":graphql-gateway")
include(":model-contracts")
include(":common-service")
include(":library-service")
include(":organization-management")
include(":api-tests")
// include(":embedded-keycloak")
