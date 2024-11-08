plugins {
    `kubernetes-conventions`
}

dependencies {
    ksp(mn.micronaut.data.processor)
    implementation(mn.micronaut.data.model)
}
