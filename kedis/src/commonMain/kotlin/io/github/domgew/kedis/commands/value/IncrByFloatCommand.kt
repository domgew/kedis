package io.github.domgew.kedis.commands.value

import io.github.domgew.kedis.KedisException
import io.github.domgew.kedis.commands.KedisFullCommand
import io.github.domgew.kedis.impl.RedisMessage

// see https://redis.io/commands/incrbyfloat/
internal class IncrByFloatCommand(
    val key: String,
    val by: Double,
) : KedisFullCommand<Double> {
    override fun fromRedisResponse(response: RedisMessage): Double =
        when {
            response is RedisMessage.StringMessage
                && response.value.toDoubleOrNull() != null ->
                response.value.toDouble()

            response is RedisMessage.StringMessage ->
                throw KedisException.WrongResponseException(
                    message = "Expected double precision floating point number, was \"${response.value}\"",
                )

            response is RedisMessage.ErrorMessage ->
                handleRedisErrorResponse(
                    response = response,
                )

            else ->
                throw KedisException.WrongResponseException(
                    message = "Expected string response, was ${response::class}",
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
        internal const val OPERATION_NAME = "INCRBYFLOAT"
    }
}
