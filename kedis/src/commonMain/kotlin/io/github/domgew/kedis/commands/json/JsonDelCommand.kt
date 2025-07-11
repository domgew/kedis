package io.github.domgew.kedis.commands.json

import io.github.domgew.kedis.KedisException
import io.github.domgew.kedis.commands.KedisFullCommand
import io.github.domgew.kedis.impl.RedisMessage

// // see https://redis.io/commands/json.del/
internal class JsonDelCommand(
    val key: String,
    val path: String?,
) : KedisFullCommand<Long?> {

    override fun fromRedisResponse(
        response: RedisMessage,
    ): Long? =
        when (response) {
            RedisMessage.NullMessage ->
                null

            is RedisMessage.IntegerMessage ->
                response.value

            is RedisMessage.ErrorMessage ->
                handleRedisErrorResponse(
                    response = response,
                )

            else ->
                throw KedisException.WrongResponseException(
                    message = "Expected integer or null response, was ${response::class.simpleName}",
                )
        }

    override fun toRedisRequest(): RedisMessage =
        RedisMessage.ArrayMessage(
            value = listOfNotNull(
                RedisMessage.BulkStringMessage(OPERATION_NAME),
                RedisMessage.BulkStringMessage(key),
                path
                    ?.let {
                        RedisMessage.BulkStringMessage(it)
                    },
            ),
        )

    companion object {

        private const val OPERATION_NAME = "JSON.DEL"
    }
}
