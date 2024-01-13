import org.jetbrains.kotlin.konan.target.HostManager

plugins {
    kotlin("multiplatform") apply false
}

// https://github.com/Dominaezzz/kotlin-sqlite/blob/master/build.gradle.kts
subprojects {
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

//        println("#####################################")
//        println("overallPublishTasks:")
//        for (task in tasks.withType<PublishToMavenRepository>()) {
//            println("\t${task.name}")
//        }
//        println()
//        println("platformPublishTasks:")
//        for (task in publishTasks) {
//            println("\t${task.name}")
//        }
//        println("#####################################")

        tasks.register("smartPublish") {
            dependsOn(publishTasks)
        }
    }
}
