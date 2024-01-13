import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget

val kotlinLoggingVersion: String by project
val ktorVersion: String by project

plugins {
    kotlin("multiplatform")
}

kotlin {
    jvm {
        withJava()
    }
    addNativeTargets {
        binaries {
            executable {
                entryPoint = "main"
            }
        }
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(project(":kedis"))

                implementation("io.ktor:ktor-server-core:$ktorVersion")
                implementation("io.ktor:ktor-server-cio:$ktorVersion")

                implementation("io.github.oshai:kotlin-logging:$kotlinLoggingVersion")
            }
        }
        val nativeMain by creating {}
        val jvmMain by getting {
            dependencies {
                implementation("ch.qos.logback:logback-classic:1.4.14")
            }
        }
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
