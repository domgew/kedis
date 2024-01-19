import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.konan.target.HostManager

val bigNumVersion: String by project
val kotlinCoroutinesVersion: String by project
val kotlinLoggingVersion: String by project
val ktorVersion: String by project

plugins {
    kotlin("multiplatform")
    `maven-publish`
}

group = "io.github.domgew"
version = "0.0.1"

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

            tasks.withType<Test> {
                environment(
                    "REDIS_PORT",
                    System.getProperty("REDIS_PORT") ?: "6379",
                )
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
        }
    }

    repositories {
        if (System.getenv("IS_CI") != "yes") {
            mavenLocal()
        } else {
            maven {
                name = "GitHubPackages"
                url = uri("https://maven.pkg.github.com/domgew/kedis")
            }
        }
    }
}

// smartPublish as per https://github.com/Dominaezzz/kotlin-sqlite/blob/master/build.gradle.kts
afterEvaluate {
    val publishTasks = tasks.withType<PublishToMavenRepository>()
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

//    println("#####################################")
//    println("publish tasks:")
//    for (task in tasks.withType<PublishToMavenRepository>()) {
//        println("\t${task.name}")
//    }
//    println()
//    println("smartPublish tasks:")
//    for (task in publishTasks) {
//        println("\t${task.name}")
//    }
//    println("#####################################")

    tasks.register("smartPublish") {
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
