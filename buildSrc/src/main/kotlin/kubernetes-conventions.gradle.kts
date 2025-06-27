import com.bmuschko.gradle.docker.shaded.org.apache.commons.lang3.RandomStringUtils
import com.bmuschko.gradle.docker.tasks.image.DockerBuildImage
import io.micronaut.gradle.docker.MicronautDockerfile
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.io.FileInputStream
import java.util.*

plugins {
    id("kotlin-conventions")
    id("testing-conventions")
    id("micronaut-conventions")
    io.micronaut.docker
    alias(libs.plugins.ksp)
    // disabled by https://github.com/JetBrains/kotlin/commit/e6cb3bee90e8b6eb255baf7c814f213a65537291
    // NoSuchMethodError: 'void org.jetbrains.kotlin.gradle.dsl.KaptArguments.arg(java.lang.Object, java.lang.Object[])
    // org.jetbrains.kotlin.kapt
}

dependencies {
    // kapt(libs.dekorate.kubernetes.annotations)
    // kapt(libs.dekorate.prometheus.annotations)

    implementation(project(":model-contracts"))
    implementation(project(":common-service"))

    compileOnly(libs.dekorate.kubernetes.annotations)
    compileOnly(libs.dekorate.prometheus.annotations)
}

val generatedTag: String get() =
    project.findProperty("generated-tag")?.toString() ?: RandomStringUtils.randomNumeric(4)

val localProperties = Properties().apply { load(FileInputStream(".run/local.properties")) }

val activeProfile: String = project.gradle.startParameter.projectProperties["active.profile"] ?: localProperties.getProperty("active.profile")
val gitCommitHash: String get() = providers.of(GitCommitValueSource::class) {}.get()
val version = "$gitCommitHash-$generatedTag"

tasks.withType<MicronautDockerfile> {
    exposePort(9000, 8080, 5005)
    args(
        "-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005",
        "-Dlogback.configurationFile=/home/app/resources/logback.xml"
    )
}

tasks.withType<Jar> {
    val moduleName = project.name.removeSuffix("-service").replace('-', '.')
    manifest.attributes["Main-Class"] = "io.eordie.multimodule.$moduleName.ApplicationKt"
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

val generateMicroConfig by tasks.registering(Exec::class) {
    workingDir = project.parent?.projectDir!!
    val args = listOf(
        "java", "-DgeneratedTag=$version",
        "-jar", "microconfig.jar",
        "-r", project.parent?.projectDir,
        "-envs", activeProfile,
        "-s", project.name,
        "-d", layout.buildDirectory.dir("microconfig").get()
    )

    commandLine(args)
}

val movingResources by tasks.registering(Copy::class) {
    dependsOn(generateMicroConfig)
    from(layout.buildDirectory.dir("microconfig/$activeProfile/${project.name}"))
    into(layout.buildDirectory.dir("resources/main"))
}

tasks.withType(KotlinCompile::class.java).configureEach {
    dependsOn(movingResources)
}

tasks.withType<DockerBuildImage> {
    outputs.upToDateWhen { false }
    dependsOn(movingResources)
    images.value(listOf("galadriel.box:32000/example/${project.name}:$version"))
}
