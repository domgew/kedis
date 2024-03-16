package io.github.domgew.kedis.commands.value

import io.github.domgew.kedis.KedisException
import io.github.domgew.kedis.commands.KedisFullCommand
import io.github.domgew.kedis.impl.RedisMessage

// see https://redis.io/commands/del/
internal class DelCommand(
    val keys: List<String>,
) : KedisFullCommand<Long> {
    override fun fromRedisResponse(response: RedisMessage): Long =
        when (response) {
            is RedisMessage.IntegerMessage ->
                response.value

            is RedisMessage.ErrorMessage ->
                handleRedisErrorResponse(
                    response = response,
                )

            else ->
                throw KedisException.WrongResponseException(
                    message = "Expected integer response, was ${response::class.simpleName}",
                )
        }

    override fun toRedisRequest(): RedisMessage =
        RedisMessage.ArrayMessage(
            value = listOf(
                RedisMessage.BulkStringMessage(OPERATION_NAME),
                *keys
                    .map { RedisMessage.BulkStringMessage(it) }
                    .toTypedArray(),
            ),
        )

    companion object {
        private const val OPERATION_NAME = "DEL"
    }
}
