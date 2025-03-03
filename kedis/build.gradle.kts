import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.konan.target.HostManager
import java.util.regex.Pattern

plugins {
    alias(libs.plugins.dokka)
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kover)
    alias(libs.plugins.serialization)
    `maven-publish`
    signing
}

group = "io.github.domgew"
version = "0.0.1-SNAPSHOT"

val commitTagPattern =
    Pattern.compile(
        "^(\\d+)\\.(\\d+)\\.(\\d+)(-([a-z]+)(\\d+))?$",
    )!!
val commitTag = System.getenv("CI_COMMIT_TAG")
    ?.trim()
    ?.ifEmpty { null }
    ?.takeIf {
        commitTagPattern.asMatchPredicate()
            .test(it)
    }

if (commitTag != null) {
    version = commitTag
}

kotlin {
    explicitApi()
    withSourcesJar(
        publish = true,
    )
    jvmToolchain(17)
    compilerOptions {
        freeCompilerArgs.add("-Xexpect-actual-classes")
    }

    jvm {
    }
    addNativeTargets {
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(libs.kotlin.stdlib)
                api(libs.kotlinx.coroutines.core)
                implementation(libs.ktor.network)
                api(libs.bignum)
                api(libs.kotlinx.serialization.json)
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(libs.kotlin.test)
                implementation(libs.kotlinx.coroutines.test)
                implementation(libs.kotlinx.datetime)
                implementation(libs.semVer)
                implementation(libs.kotlinx.serialization.json)
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
    mingwX64 {
        block()
    }
}

val dokkaOutputDir = layout.buildDirectory.dir("dokka")
dokka {
    dokkaPublications.html {
        outputDirectory.set(dokkaOutputDir)
    }
}
val deleteDokkaOutputDir by tasks.register<Delete>("deleteDokkaOutputDirectory") {
    this.delete(dokkaOutputDir)
}
val javadocJar = tasks.register<Jar>("javadocJar") {
    dependsOn(
        deleteDokkaOutputDir,
        tasks.getByName("dokkaGeneratePublicationHtml"),
    )
    archiveClassifier.set("javadoc")
    from(dokkaOutputDir)
}

publishing {
    publications {
        withType<MavenPublication> {
            artifact(javadocJar)
            pom {
                name.set("Kedis")
                description.set("Redis client library for Kotlin Multiplatform (JVM + Native)")
                url.set("https://github.com/domgew/kedis")
                scm {
                    url.set("https://github.com/domgew/kedis")
                    connection.set("scm:git:git://github.com/domgew/kedis.git")
                    developerConnection.set("scm:git:ssh://github.com:domgew/kedis.git")
                }
                licenses {
                    license {
                        name.set("MIT")
                        url.set("https://opensource.org/licenses/MIT")
                    }
                }
                issueManagement {
                    system.set("Github")
                    url.set("https://github.com/domgew/kedis/issues")
                }
                developers {
                    developer {
                        name.set("domgew")
                        email.set("44265359+domgew@users.noreply.github.com")
                    }
                }
            }
        }
    }

    repositories {
        if (System.getenv("IS_CI") != "yes") {
            mavenLocal()
        } else {
            // see https://medium.com/kodein-koders/publish-a-kotlin-multiplatform-library-on-maven-central-6e8a394b7030
            maven {
                name = "oss"

                val releasesRepoUrl = uri(
                    "https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/",
                )
                val snapshotsRepoUrl = uri("https://s01.oss.sonatype.org/content/repositories/snapshots/")
                url =
                    if (
                        version.toString()
                            .endsWith("SNAPSHOT")
                    )
                        snapshotsRepoUrl
                    else
                        releasesRepoUrl

                credentials {
                    username = System.getenv("SONATYPE_USER")
                        ?.trim()
                        ?.ifEmpty { null }
                    password = System.getenv("SONATYPE_PASS")
                        ?.trim()
                        ?.ifEmpty { null }
                }
            }
        }
    }
}

signing {
    useInMemoryPgpKeys(
        System.getenv("GPG_PRIVATE_KEY"),
        System.getenv("GPG_PRIVATE_PASSWORD"),
    )
    sign(publishing.publications)
}

// https://github.com/gradle/gradle/issues/26091
val signingTasks = tasks.withType<Sign>()
tasks.withType<AbstractPublishToMaven>().configureEach {
    dependsOn(signingTasks)
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
                        || it.name.startsWith("publishMingw")
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
