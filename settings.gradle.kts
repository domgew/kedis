pluginManagement {
    val kotlinVersion: String by settings
    val foojayVersion: String by settings

    repositories {
        gradlePluginPortal()
        mavenCentral()
        google()
        maven("https://jitpack.io/")
        mavenLocal()
    }

    plugins {
        kotlin("multiplatform") version kotlinVersion apply false
        id("org.gradle.toolchains.foojay-resolver-convention") version foojayVersion apply false
    }
}

@Suppress("UnstableApiUsage")
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        mavenCentral()
        google()
        maven("https://jitpack.io/")
        mavenLocal()
    }
}

rootProject.name = "kredis"
