plugins {
    `kubernetes-conventions`
}

dependencies {
    implementation("io.micronaut.redis:micronaut-redis-lettuce")
    implementation("io.micronaut.views:micronaut-views-thymeleaf")
    implementation("io.micronaut.graphql:micronaut-graphql")
    implementation("io.micronaut.security:micronaut-security-jwt")
    implementation("io.micronaut.security:micronaut-security-oauth2")

    implementation(libs.kotlin.graphql)
    implementation(libs.graphql.scalars)
}
