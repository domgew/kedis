package io.github.domgew.kedis.commands.hash

import io.github.domgew.kedis.KedisException
import io.github.domgew.kedis.commands.KedisFullCommand
import io.github.domgew.kedis.impl.RedisMessage

// see https://redis.io/commands/hdel/
internal class HashDelCommand(
    val key: String,
    val fields: List<String>,
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
                RedisMessage.BulkStringMessage(key),
                *fields
                    .map { RedisMessage.BulkStringMessage(it) }
                    .toTypedArray(),
            ),
        )

    companion object {
        private const val OPERATION_NAME = "HDEL"
    }
}
