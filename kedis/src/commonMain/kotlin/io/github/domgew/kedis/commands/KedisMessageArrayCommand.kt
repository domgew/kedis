package io.github.domgew.kedis.commands

import io.github.domgew.kedis.impl.RedisMessage

internal data class KedisMessageArrayCommand(
    val messages: List<RedisMessage>,
): KedisCommand {
    override fun toRedisMessage(): RedisMessage =
        RedisMessage.ArrayMessage(
            value = messages,
        )
}
