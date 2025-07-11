package io.github.domgew.kedis.commands.json

import io.github.domgew.kedis.KedisException
import io.github.domgew.kedis.commands.KedisFullCommand
import io.github.domgew.kedis.impl.RedisMessage

// // see https://redis.io/commands/json.toggle/
internal class JsonToggleCommand(
    val key: String,
    val path: String,
) : KedisFullCommand<List<Boolean?>?> {

    override fun fromRedisResponse(
        response: RedisMessage,
    ): List<Boolean?>? =
        when (response) {
            RedisMessage.NullMessage ->
                null

            is RedisMessage.ArrayMessage ->
                response.value
                    .map {
                        when (it) {
                            RedisMessage.NullMessage ->
                                null

                            is RedisMessage.BooleanMessage ->
                                it.value

                            is RedisMessage.IntegerMessage ->
                                when (it.value) {
                                    0L ->
                                        false

                                    1L ->
                                        true

                                    else ->
                                        throw KedisException.WrongResponseException(
                                            message = "Expected integer item to be 0 or 1, was ${it.value}",
                                        )
                                }

                            else ->
                                throw KedisException.WrongResponseException(
                                    message = "Expected item to be of type boolean, integer, or null," +
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
                    message = "Expected array or null response, was ${response::class.simpleName}",
                )
        }

    override fun toRedisRequest(): RedisMessage =
        RedisMessage.ArrayMessage(
            value = listOf(
                RedisMessage.BulkStringMessage(OPERATION_NAME),
                RedisMessage.BulkStringMessage(key),
                RedisMessage.BulkStringMessage(path),
            ),
        )

    companion object {

        private const val OPERATION_NAME = "JSON.TOGGLE"
    }
}
