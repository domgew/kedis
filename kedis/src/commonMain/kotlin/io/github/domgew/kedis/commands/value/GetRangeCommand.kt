package io.github.domgew.kedis.commands.value

import io.github.domgew.kedis.KedisException
import io.github.domgew.kedis.commands.KedisFullCommand
import io.github.domgew.kedis.impl.RedisMessage

// see https://redis.io/commands/getrange/
internal class GetRangeCommand(
    val key: String,
    val start: Long,
    val end: Long,
) : KedisFullCommand<String> {
    override fun fromRedisResponse(response: RedisMessage): String =
        when (response) {
            is RedisMessage.StringMessage ->
                response.value

            is RedisMessage.ErrorMessage ->
                handleRedisErrorResponse(
                    response = response,
                )

            else ->
                throw KedisException.WrongResponseException(
                    message = "Expected string or null response, was ${response::class}",
                )
        }

    override fun toRedisRequest(): RedisMessage =
        RedisMessage.ArrayMessage(
            value = listOf(
                RedisMessage.BulkStringMessage(OPERATION_NAME),
                RedisMessage.BulkStringMessage(key),
                RedisMessage.BulkStringMessage(start.toString()),
                RedisMessage.BulkStringMessage(end.toString()),
            ),
        )

    companion object {
        private const val OPERATION_NAME = "GETRANGE"
    }
}
