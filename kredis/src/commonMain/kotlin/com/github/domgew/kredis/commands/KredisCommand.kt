package com.github.domgew.kredis.commands

import com.github.domgew.kredis.impl.RedisMessage

internal interface KredisCommand {
    fun toRedisMessage(): RedisMessage
}
