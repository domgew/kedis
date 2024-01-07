plugins {
    kotlin("multiplatform")
}

group = "com.github.domgew"
version = "0.0.1"

kotlin {
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
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }
    }
}
