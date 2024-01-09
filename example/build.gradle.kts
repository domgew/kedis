val kotlinLoggingVersion: String by project
val ktorVersion: String by project

plugins {
    kotlin("multiplatform")
}

kotlin {
    jvm {
        withJava()
    }
    linuxX64 {
        binaries {
            executable {
                entryPoint = "main"
            }
        }
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(project(":kredis"))

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
