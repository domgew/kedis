pluginManagement {
    val kotlinVersion: String by settings
    val dokkaVersion: String by settings

    repositories {
        gradlePluginPortal()
        mavenCentral()
        google()
        maven("https://jitpack.io/")
        mavenLocal()
    }

    plugins {
        kotlin("multiplatform") version kotlinVersion apply false
        id("org.jetbrains.dokka") version dokkaVersion apply false
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
        maven("https://jitpack.io/")

        // workaround for https://youtrack.jetbrains.com/issue/KT-51379
        exclusiveContent {
            forRepository {
                ivy("https://download.jetbrains.com/kotlin/native/builds") {
                    name = "Kotlin Native"
                    patternLayout {

                        // example download URLs:
                        // https://download.jetbrains.com/kotlin/native/builds/releases/1.7.20/linux-x86_64/kotlin-native-prebuilt-linux-x86_64-1.7.20.tar.gz
                        // https://download.jetbrains.com/kotlin/native/builds/releases/1.7.20/windows-x86_64/kotlin-native-prebuilt-windows-x86_64-1.7.20.zip
                        // https://download.jetbrains.com/kotlin/native/builds/releases/1.7.20/macos-x86_64/kotlin-native-prebuilt-macos-x86_64-1.7.20.tar.gz
                        listOf(
                            "macos-x86_64",
                            "macos-aarch64",
                            "osx-x86_64",
                            "osx-aarch64",
                            "linux-x86_64",
                            "windows-x86_64",
                        ).forEach { os ->
                            listOf("dev", "releases").forEach { stage ->
                                artifact("$stage/[revision]/$os/[artifact]-[revision].[ext]")
                            }
                        }
                    }
                    metadataSources { artifact() }
                }
            }
            filter { includeModuleByRegex(".*", ".*kotlin-native-prebuilt.*") }
        }

        mavenLocal()
    }
}

rootProject.name = "kedis"

include(":kedis")

if (System.getenv("IS_CI") != "yes") {
    include(":example")
}
