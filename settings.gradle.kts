pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        google()
    }
}

@Suppress("UnstableApiUsage")
dependencyResolutionManagement {
    if (System.getenv("IS_CI") == "yes") {
        repositoriesMode.set(RepositoriesMode.PREFER_SETTINGS)
    } else {
        repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    }
    repositories {
        mavenCentral()
        google()
    }
}

rootProject.name = "kedis"

include(":kedis")
include(":kedis-lock")

if (System.getenv("IS_CI") != "yes") {
    include(":example")
}
