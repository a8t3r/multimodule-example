import io.micronaut.testresources.buildtools.KnownModules

plugins {
    `kubernetes-conventions`
    id("io.micronaut.test-resources")
}

dependencies {
    ksp("io.micronaut.data:micronaut-data-processor")
    implementation(project(":model-contracts"))
    implementation(project(":common-service"))
}

micronaut {
    testResources {
        additionalModules.add(KnownModules.JDBC_POSTGRESQL)
    }
}
