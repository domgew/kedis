package io.github.domgew.kedis.commands.server

import io.github.domgew.kedis.KedisException
import io.github.domgew.kedis.commands.KedisFullCommand
import io.github.domgew.kedis.impl.RedisMessage

internal class PingCommand(
    val content: String,
) : KedisFullCommand<String> {
    override fun fromRedisResponse(response: RedisMessage): String =
        when {
            response is RedisMessage.StringMessage ->
                response.value

            else ->
                throw KedisException.WrongResponse(
                    message = "Expected string response, was ${response::class}",
                )
        }

    override fun toRedisRequest(): RedisMessage =
        RedisMessage.ArrayMessage(
            value = listOf(
                RedisMessage.BulkStringMessage(OPERATION_NAME),
                RedisMessage.BulkStringMessage(content),
            ),
        )

    companion object {
        private const val OPERATION_NAME = "PING"
    }
}
