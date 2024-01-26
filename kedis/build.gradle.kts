import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.konan.target.HostManager
import java.util.regex.Pattern

val bigNumVersion: String by project
val kotlinCoroutinesVersion: String by project
val kotlinLoggingVersion: String by project
val ktorVersion: String by project

plugins {
    kotlin("multiplatform")
    `maven-publish`
}

group = "io.github.domgew"
version = "0.0.1-SNAPSHOT"

val commitTagPattern = Pattern.compile(
    "^(\\d+)\\.(\\d+)\\.(\\d+)(-([a-z]+)(\\d+))?$",
)!!
val commitTag = System.getenv("CI_COMMIT_TAG")
    ?.trim()
    ?.ifEmpty { null }
    ?.takeIf { commitTagPattern.asMatchPredicate().test(it) }

if (commitTag != null) {
    version = commitTag
}

kotlin {
    explicitApi()
    withSourcesJar(
        publish = true,
    )

    jvm {
        jvmToolchain(17)
    }
    addNativeTargets {
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(kotlin("stdlib"))

                api("org.jetbrains.kotlinx:kotlinx-coroutines-core:$kotlinCoroutinesVersion")
                implementation("io.ktor:ktor-network:$ktorVersion")

                api("com.ionspin.kotlin:bignum:$bigNumVersion")
                api("io.github.oshai:kotlin-logging:$kotlinLoggingVersion")
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:$kotlinCoroutinesVersion")
            }
        }
    }
}

publishing {
    publications.withType<MavenPublication> {
        pom {
            name.set("Kedis")
            description.set("Redis client library for Kotlin Multiplatform (JVM + Native)")
            url.set("https://github.com/domgew/kedis")
            scm {
                url.set("https://github.com/domgew/kedis")
            }
            licenses {
                license {
                    name.set("MIT License")
                    url.set("https://github.com/domgew/kedis/blob/development/LICENSE")
                }
            }
        }
    }

    repositories {
        if (System.getenv("IS_CI") != "yes") {
            mavenLocal()
        } else {
            maven {
                name = "GitHubPackages"
                url = uri("https://maven.pkg.github.com/domgew/kedis")
                credentials {
                    username = "domgew"
                    password = System.getenv("GH_TOKEN")
                }
            }
        }
    }
}

// smartPublish as per https://github.com/Dominaezzz/kotlin-sqlite/blob/master/build.gradle.kts
afterEvaluate {
    val testTasks = project.tasks.withType<AbstractTestTask>()
        .matching {
            when {
                HostManager.hostIsMingw ->
                    it.name.startsWith("mingw", true)

                HostManager.hostIsMac ->
                    it.name.startsWith("macos", true)

                HostManager.hostIsLinux ->
                    it.name.startsWith("linux", true)
                            || it.name.startsWith("js", true)
                            || it.name.startsWith("jvm", true)

                else ->
                    throw Exception("unknown host")
            }
        }
    val publishTasks = project.tasks.withType<PublishToMavenRepository>()
        .matching {
            when {
                HostManager.hostIsMingw ->
                    it.name.startsWith("publishMingw")

                HostManager.hostIsMac ->
                    it.name.startsWith("publishMacos")

                HostManager.hostIsLinux ->
                    it.name.startsWith("publishLinux")
                            || it.name.startsWith("publishJs")
                            || it.name.startsWith("publishJvmPublication")
                            || it.name.startsWith("publishMetadata")
                            || it.name.startsWith("publishKotlinMultiplatform")

                else -> throw Exception("unknown host")
            }
        }

    if (System.getenv("IS_CI") == "yes") {
        println("#####################################")
        println("test tasks:")
        for (task in project.tasks.withType<AbstractTestTask>()) {
            println("\t${task.name}")
        }
        println()
        println("smartTest tasks:")
        for (task in testTasks) {
            println("\t${task.name}")
        }
        println("#####################################")
        println("publish tasks:")
        for (task in project.tasks.withType<PublishToMavenRepository>()) {
            println("\t${task.name}")
        }
        println()
        println("smartPublish tasks:")
        for (task in publishTasks) {
            println("\t${task.name}")
        }
        println("#####################################")
    }

    project.tasks.register("smartTest") {
        dependsOn(testTasks)
    }
    project.tasks.register("smartPublish") {
        dependsOn(publishTasks)
    }
}

fun KotlinMultiplatformExtension.addNativeTargets(
    block: KotlinNativeTarget.() -> Unit,
) {
    linuxX64 {
        block()
    }
    linuxArm64 {
        block()
    }
    macosX64 {
        block()
    }
    macosArm64 {
        block()
    }
}
