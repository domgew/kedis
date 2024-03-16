package io.github.domgew.kedis.commands.hash

import io.github.domgew.kedis.KedisException
import io.github.domgew.kedis.commands.KedisFullCommand
import io.github.domgew.kedis.impl.RedisMessage

// // see https://redis.io/commands/hgetall/
internal class HashGetAllBinaryCommand(
    val key: String,
) : KedisFullCommand<Map<String, ByteArray>?> {
    override fun fromRedisResponse(response: RedisMessage): Map<String, ByteArray>? =
        when (response) {
            is RedisMessage.ArrayMessage ->
                when {
                    response.value.isEmpty() ->
                        null

                    response.value.size % 2 != 0 ->
                        throw KedisException.WrongResponseException(
                            message = "Expected an even number of elements, got ${response.value.size}",
                        )

                    else ->
                        response.value
                            .let { items ->
                                (0 until (items.size / 2))
                                    .associate { i ->
                                        val keyItem = items[i * 2]
                                        val valueItem = items[i * 2 + 1]
                                        val key = if (keyItem is RedisMessage.StringMessage) {
                                            keyItem.value
                                        } else {
                                            throw KedisException.WrongResponseException(
                                                message = "Expected item key to be of type string," +
                                                    " was ${keyItem::class.simpleName}",
                                            )
                                        }
                                        val value = if (valueItem is RedisMessage.StringMessage) {
                                            valueItem.data
                                        } else {
                                            throw KedisException.WrongResponseException(
                                                message = "Expected item value to be of type string," +
                                                    " was ${valueItem::class.simpleName}",
                                            )
                                        }

                                        return@associate key to value
                                    }
                            }
                }

            is RedisMessage.MessageMapMessage ->
                response.value
                    .takeIf { it.isNotEmpty() }
                    ?.entries
                    ?.associate {
                        val keyItem = it.key
                        val valueItem = it.value
                        val key = if (keyItem is RedisMessage.StringMessage) {
                            keyItem.value
                        } else {
                            throw KedisException.WrongResponseException(
                                message = "Expected item key to be of type string," +
                                    " was ${keyItem::class.simpleName}",
                            )
                        }
                        val value = if (valueItem is RedisMessage.StringMessage) {
                            valueItem.data
                        } else {
                            throw KedisException.WrongResponseException(
                                message = "Expected item value to be of type string," +
                                    " was ${valueItem::class.simpleName}",
                            )
                        }

                        return@associate key to value
                    }
                    ?: throw KedisException.WrongResponseException(
                        message = "Expected items to be of type string",
                    )

            is RedisMessage.NullMessage ->
                null

            is RedisMessage.ErrorMessage ->
                handleRedisErrorResponse(
                    response = response,
                )

            else ->
                throw KedisException.WrongResponseException(
                    message = "Expected array, map, or null response, was ${response::class.simpleName}",
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
        private const val OPERATION_NAME = "HGETALL"
    }
}
