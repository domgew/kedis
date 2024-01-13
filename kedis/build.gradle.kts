import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.konan.target.HostManager

val bigNumVersion: String by project
val kotlinCoroutinesVersion: String by project
val kotlinLoggingVersion: String by project
val ktorVersion: String by project

plugins {
    kotlin("multiplatform")
    id("maven-publish")
}

group = "io.github.domgew"
version = "0.0.1"

kotlin {
    explicitApi()

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
    repositories {
        mavenLocal()
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
//    println("overallPublishTasks:")
//    for (task in tasks.withType<PublishToMavenRepository>()) {
//        println("\t${task.name}")
//    }
//    println()
//    println("platformPublishTasks:")
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
