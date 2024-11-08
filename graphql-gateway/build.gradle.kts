plugins {
    `kubernetes-conventions`
}

dependencies {
    implementation(mn.micronaut.redis.lettuce)
    implementation(mn.micronaut.views.thymeleaf)
    implementation(mn.micronaut.graphql)
    implementation(mn.micronaut.security.jwt)
    implementation(mn.micronaut.security.oauth2)

    implementation(libs.kotlin.graphql)
    implementation(libs.graphql.scalars)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlin.graphql.persisted.queries)
}
