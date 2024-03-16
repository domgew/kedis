package io.github.domgew.kedis.commands.hash

import io.github.domgew.kedis.KedisException
import io.github.domgew.kedis.commands.KedisFullCommand
import io.github.domgew.kedis.impl.RedisMessage

// see https://redis.io/commands/hset/
internal class HashSetBinaryCommand(
    val key: String,
    val fieldValues: Map<String, ByteArray>,
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
                    message = "Expected string response, was ${response::class.simpleName}",
                )
        }

    override fun toRedisRequest(): RedisMessage =
        RedisMessage.ArrayMessage(
            value = listOf(
                RedisMessage.BulkStringMessage(OPERATION_NAME),
                RedisMessage.BulkStringMessage(key),
                *fieldValues
                    .flatMap {
                        listOf(
                            RedisMessage.BulkStringMessage(it.key),
                            RedisMessage.BulkStringMessage(it.value),
                        )
                    }
                    .toTypedArray(),
            ),
        )

    companion object {
        internal const val OPERATION_NAME = "HSET"
    }
}
