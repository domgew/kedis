package io.github.domgew.kedis.commands.value

import io.github.domgew.kedis.KedisException
import io.github.domgew.kedis.commands.KedisFullCommand
import io.github.domgew.kedis.impl.RedisMessage

// see https://redis.io/commands/incrby/
internal class IncrByCommand(
    val key: String,
    val by: Long,
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
                    message = "Expected integer response, was ${response::class}",
                )
        }

    override fun toRedisRequest(): RedisMessage =
        RedisMessage.ArrayMessage(
            value = listOf(
                RedisMessage.BulkStringMessage(OPERATION_NAME),
                RedisMessage.BulkStringMessage(key),
                RedisMessage.BulkStringMessage(by.toString()),
            ),
        )

    companion object {
        internal const val OPERATION_NAME = "INCRBY"
    }
}
