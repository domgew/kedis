package io.github.domgew.kedis.commands

import io.github.domgew.kedis.impl.RedisMessage

internal interface KedisCommand {
    fun toRedisRequest(): RedisMessage
}
