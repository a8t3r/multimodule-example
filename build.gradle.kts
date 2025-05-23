plugins {
    idea
    id("com.autonomousapps.dependency-analysis")
    id("com.github.ben-manes.versions") version "0.51.0"
    id("nl.littlerobots.version-catalog-update") version "1.0.0"
}

idea {
    module.isDownloadJavadoc = true
    module.isDownloadSources = true
}
