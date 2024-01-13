package com.github.domgew.kedis.commands

import com.github.domgew.kedis.impl.RedisMessage

internal data class KedisStringArrayCommand(
    val values: List<String>,
): KedisCommand {
    override fun toRedisMessage(): RedisMessage =
        RedisMessage.ArrayMessage(
            value = values.map {
                RedisMessage.BulkStringMessage(
                    value = it,
                )
            },
        )
}
