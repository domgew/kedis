val bigNumVersion: String by project
val kotlinCoroutinesVersion: String by project
val kotlinLoggingVersion: String by project
val ktorVersion: String by project

plugins {
    kotlin("multiplatform")
}

group = "com.github.domgew"
version = "0.0.1"

kotlin {
    explicitApi()

    jvm {
        jvmToolchain(17)
    }
    linuxX64 {
        binaries {
            sharedLib {}
        }
    }
    linuxArm64{
        binaries {
            sharedLib {}
        }
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
