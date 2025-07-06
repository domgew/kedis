package io.github.domgew.kedis.utils

import io.github.domgew.kedis.KedisClient
import io.github.domgew.kedis.arguments.server.InfoSectionName
import io.github.domgew.kedis.commands.KedisServerCommands
import io.github.domgew.kedis.results.server.InfoSection
import net.swiftzer.semver.SemVer

suspend fun KedisClient.getRedisVersion(): SemVer? =
    execute(
        command = KedisServerCommands.info(
            InfoSectionName.SERVER,
        ),
    )
        .filterIsInstance<InfoSection.Server>()
        .firstOrNull()
        ?.redisVersion
        ?.let {
            SemVer.parseOrNull(it)
        }

suspend fun KedisClient.hasJsonSupport(): Boolean {
    val regex = Regex("^module:name=([^,]+),")

    return execute(
        command = KedisServerCommands.infoRaw(
            InfoSectionName.MODULES,
        ),
    )
        ?.split("\n")
        ?.any {
            regex.find(it)
                ?.groupValues
                ?.getOrNull(1)
                ?.contains(
                    "json",
                    ignoreCase = true,
                )
                ?: false
        }
        ?: false
}
