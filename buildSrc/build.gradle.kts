val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")

plugins {
    `kotlin-dsl`
}

repositories {
    gradlePluginPortal()
    mavenCentral()
}

dependencies {
    add("implementation", libs.findLibrary("ksp-gradle").get())
    add("implementation", libs.findLibrary("kotlin-gradle").get())
    add("implementation", libs.findLibrary("kotlin-allopen").get())
    add("implementation", libs.findLibrary("kotlin-noarg").get())
    add("implementation", libs.findLibrary("detekt-gradle").get())
    add("implementation", libs.findLibrary("dag-command-gradle").get())
    add("implementation", libs.findLibrary("micronaut-gradle").get())
    add("implementation", libs.findLibrary("kotlinx-serialization-gradle").get())
}
