package io.github.domgew.kedis.commands

import io.github.domgew.kedis.impl.RedisMessage

internal data class KedisStringArrayCommand(
    val values: List<String>,
): KedisCommand {
    override fun toRedisRequest(): RedisMessage =
        RedisMessage.ArrayMessage(
            value = values.map {
                RedisMessage.BulkStringMessage(
                    value = it,
                )
            },
        )
}
