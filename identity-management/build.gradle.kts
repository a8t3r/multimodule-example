plugins {
    id("kotlin-conventions")
    id("testing-conventions")
    id("dokka-conventions")

    id("org.springframework.boot") version "2.6.7"
    id("io.spring.dependency-management") version "1.0.11.RELEASE"
}

repositories {
    mavenLocal()
}

dependencies {
    implementation(project(":model-contracts"))

    implementation("com.zaxxer:HikariCP:5.0.1")
    implementation("org.postgresql:postgresql:42.5.0")

    implementation("ch.qos.logback:logback-classic:1.2.11")
    implementation("org.slf4j:slf4j-api:1.7.36")

    implementation("org.springframework.boot:spring-boot-starter-jdbc")
    implementation("org.springframework.boot:spring-boot-starter-logging")
    implementation("org.springframework.boot:spring-boot-starter-undertow")

    implementation("com.github.thomasdarimont.keycloak:embedded-keycloak-server-spring-boot-starter:10.0.0")

    implementation("io.phasetwo.keycloak:keycloak-orgs:0.6")
}
