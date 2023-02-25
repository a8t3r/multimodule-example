plugins {
    idea
    id("com.github.ben-manes.versions") version "0.46.0"
    id("nl.littlerobots.version-catalog-update") version "0.7.0"
}

idea {
    module.isDownloadJavadoc = true
    module.isDownloadSources = true
}
