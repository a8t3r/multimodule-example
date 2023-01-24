plugins {
    id("kotlin-conventions")
    id("testing-conventions")
    id("dokka-conventions")
    id("micronaut-conventions")
}

dependencies {
    implementation(project(":model-contracts"))
}
