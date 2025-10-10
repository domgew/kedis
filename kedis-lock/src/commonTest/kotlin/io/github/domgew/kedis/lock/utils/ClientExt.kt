package io.github.domgew.kedis.lock.utils

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
