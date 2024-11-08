plugins {
    idea
    id("com.github.ben-manes.versions") version "0.51.0"
    id("nl.littlerobots.version-catalog-update") version "0.8.5"
}

versionCatalogUpdate {
    keep {
        // keep versions without any library or plugin reference
        keepUnusedVersions = true
        // keep all libraries that aren't used in the project
        keepUnusedLibraries = true
        // keep all plugins that aren't used in the project
        keepUnusedPlugins = true
    }
}

idea {
    module.isDownloadJavadoc = true
    module.isDownloadSources = true
}
