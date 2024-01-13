package com.github.domgew.kedis.commands

import com.github.domgew.kedis.impl.RedisMessage

internal interface KedisCommand {
    fun toRedisMessage(): RedisMessage
}
