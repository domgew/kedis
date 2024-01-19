package io.github.domgew.kedis.commands.value

import io.github.domgew.kedis.KedisException
import io.github.domgew.kedis.commands.KedisFullCommand
import io.github.domgew.kedis.impl.RedisMessage

internal class GetCommand(
    val key: String,
): KedisFullCommand<String?> {
    override fun fromRedisResponse(response: RedisMessage): String? =
        when (response) {
            is RedisMessage.StringMessage ->
                response.value

            is RedisMessage.NullMessage ->
                null

            is RedisMessage.ErrorMessage ->
                handleRedisErrorResponse(
                    response = response,
                )

            else ->
                throw KedisException.WrongResponse(
                    message = "Expected string or null response, was ${response::class}",
                )
        }

    override fun toRedisRequest(): RedisMessage =
        RedisMessage.ArrayMessage(
            value = listOf(
                RedisMessage.BulkStringMessage(OPERATION_NAME),
                RedisMessage.BulkStringMessage(key),
            ),
        )

    companion object {
        private const val OPERATION_NAME = "GET"
    }
}
