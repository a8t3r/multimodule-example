plugins {
    idea
    id("com.autonomousapps.dependency-analysis")
    id("io.github.ermadmi78.kobby") version "4.1.1"
    id("com.github.ben-manes.versions") version "0.51.0"
    id("nl.littlerobots.version-catalog-update") version "0.8.5"
}

versionCatalogUpdate {
    pin {
        libraries = setOf(
            libs.ksp.gradle,
            libs.kotlinx.coroutines.bom,
            libs.kotlinx.serialization.json,
            libs.kotlinx.serialization.protobuf,
            libs.kotlinx.serialization.gradle,
        )
    }
    keep {
        keepUnusedVersions = true
        keepUnusedLibraries = true
        keepUnusedPlugins = true
    }
}

idea {
    module.isDownloadJavadoc = true
    module.isDownloadSources = true
}
