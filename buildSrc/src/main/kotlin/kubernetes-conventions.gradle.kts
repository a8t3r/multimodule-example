import com.bmuschko.gradle.docker.tasks.image.DockerBuildImage
import io.micronaut.gradle.docker.MicronautDockerfile

plugins {
    id("kotlin-conventions")
    id("testing-conventions")
    id("micronaut-conventions")
    id("com.google.devtools.ksp")
    `kotlin-kapt`
}

val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")
val dekorateVersion = libs.findVersion("dekorate").get().toString()
val jimmerVersion = libs.findVersion("jimmer").get().toString()

dependencies {
    ksp("org.babyfish.jimmer:jimmer-ksp:$jimmerVersion")
//    kapt("io.dekorate:kubernetes-annotations:$dekorateVersion")
//    kapt("io.dekorate:prometheus-annotations:$dekorateVersion")

    implementation("org.babyfish.jimmer:jimmer-sql-kotlin:$jimmerVersion")
    compileOnly("io.dekorate:kubernetes-annotations:$dekorateVersion")
    compileOnly("io.dekorate:prometheus-annotations:$dekorateVersion")
}

tasks.withType<MicronautDockerfile> {
    exposePort(9000)
}

tasks.withType<Jar> {
    val moduleName = project.name.removeSuffix("-service").substringAfterLast('-')
    manifest.attributes["Main-Class"] = "io.eordie.multimodule.example.$moduleName.ApplicationKt"
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

tasks.withType<DockerBuildImage> {
    images.value(listOf("registry.io:32000/${project.name}:${project.version}"))
}
