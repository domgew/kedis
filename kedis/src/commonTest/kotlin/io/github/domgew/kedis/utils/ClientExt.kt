package io.github.domgew.kedis.utils

import io.github.domgew.kedis.KedisClient
import io.github.domgew.kedis.arguments.InfoSectionName
import io.github.domgew.kedis.results.server.InfoSection
import net.swiftzer.semver.SemVer

suspend fun KedisClient.getRedisVersion(): SemVer? =
    info(InfoSectionName.SERVER)
        .filterIsInstance<InfoSection.Server>()
        .firstOrNull()
        ?.redisVersion
        ?.let {
            SemVer.parseOrNull(it)
        }
