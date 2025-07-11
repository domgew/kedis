package io.github.domgew.kedis.commands.json

import io.github.domgew.kedis.KedisException
import io.github.domgew.kedis.commands.KedisFullCommand
import io.github.domgew.kedis.impl.RedisMessage

// // see https://redis.io/commands/json.type/
internal class JsonTypeCommand(
    val key: String,
    val path: String?,
) : KedisFullCommand<List<String>?> {

    override fun fromRedisResponse(
        response: RedisMessage,
    ): List<String>? =
        when (response) {
            RedisMessage.NullMessage ->
                null

            is RedisMessage.ArrayMessage ->
                response.value
                    .map {
                        if (it is RedisMessage.StringMessage) {
                            it.value
                        } else {
                            throw KedisException.WrongResponseException(
                                message = "Expected item to be of type string," +
                                    " was ${it::class.simpleName}",
                            )
                        }
                    }

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
        private const val OPERATION_NAME = "JSON.TYPE"
    }
}
