package io.github.domgew.kedis.commands.hash

import io.github.domgew.kedis.KedisException
import io.github.domgew.kedis.commands.KedisFullCommand
import io.github.domgew.kedis.impl.RedisMessage

// see https://redis.io/commands/hexists/
internal class HashExistsCommand(
    val key: String,
    val field: String,
) : KedisFullCommand<Boolean> {
    override fun fromRedisResponse(response: RedisMessage): Boolean =
        when (response) {
            is RedisMessage.IntegerMessage ->
                response.value > 0

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
                RedisMessage.BulkStringMessage(field),
            ),
        )

    companion object {
        private const val OPERATION_NAME = "HEXISTS"
    }
}
