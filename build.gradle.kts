plugins {
    idea
    id("com.github.ben-manes.versions") version "0.51.0"
    id("nl.littlerobots.version-catalog-update") version "0.8.5"
}

idea {
    module.isDownloadJavadoc = true
    module.isDownloadSources = true
}
