package io.github.domgew.kedis.commands.value

import io.github.domgew.kedis.KedisException
import io.github.domgew.kedis.commands.KedisFullCommand
import io.github.domgew.kedis.impl.RedisMessage

// see https://redis.io/commands/get/
internal class GetBinaryCommand(
    val key: String,
) : KedisFullCommand<ByteArray?> {
    override fun fromRedisResponse(response: RedisMessage): ByteArray? =
        when (response) {
            is RedisMessage.StringMessage ->
                response.data

            is RedisMessage.NullMessage ->
                null

            is RedisMessage.ErrorMessage ->
                handleRedisErrorResponse(
                    response = response,
                )

            else ->
                throw KedisException.WrongResponseException(
                    message = "Expected string or null response, was ${response::class.simpleName}",
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
