pluginManagement {
    val kotlinVersion: String by settings

    repositories {
        gradlePluginPortal()
        mavenCentral()
        google()
        maven("https://jitpack.io/")
        mavenLocal()
    }

    plugins {
        kotlin("multiplatform") version kotlinVersion apply false
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

rootProject.name = "kedis"

include(
    ":kedis",
    ":example",
)
