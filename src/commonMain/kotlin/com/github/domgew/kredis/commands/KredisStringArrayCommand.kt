package com.github.domgew.kredis.commands

import com.github.domgew.kredis.impl.RedisMessage

internal data class KredisStringArrayCommand(
    val values: List<String>,
): KredisCommand {
    override fun toRedisMessage(): RedisMessage =
        RedisMessage.ArrayMessage(
            value = values.map {
                RedisMessage.BulkStringMessage(
                    value = it,
                )
            },
        )
}
