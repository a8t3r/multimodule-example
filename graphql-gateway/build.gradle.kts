plugins {
    `kubernetes-conventions`
}

dependencies {
    implementation(mn.micronaut.redis.lettuce)
    implementation(mn.micronaut.views.thymeleaf)
    implementation(mn.micronaut.graphql)
    implementation(mn.micronaut.websocket)
    implementation(mn.micronaut.security.jwt)
    implementation(mn.micronaut.security.oauth2)

    implementation(ktn.kotlinReflect)
    implementation(libs.kotlin.graphql.dataloader)
    implementation(libs.kotlin.graphql.generator)
    implementation(libs.graphql.scalars)
    implementation(ktx.kotlinxSerializationJson)
    implementation(libs.kotlin.graphql.persisted.queries)
}
