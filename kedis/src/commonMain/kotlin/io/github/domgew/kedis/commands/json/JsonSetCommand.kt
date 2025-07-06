package io.github.domgew.kedis.commands.json

import io.github.domgew.kedis.KedisException
import io.github.domgew.kedis.arguments.json.JsonSetOptions
import io.github.domgew.kedis.commands.KedisFullCommand
import io.github.domgew.kedis.impl.RedisMessage
import io.github.domgew.kedis.results.json.JsonSetResult

// // see https://redis.io/commands/json.set/
internal class JsonSetCommand(
    val key: String,
    val path: String,
    val value: String,
    val options: JsonSetOptions,
) : KedisFullCommand<JsonSetResult> {

    override fun fromRedisResponse(
        response: RedisMessage,
    ): JsonSetResult =
        when (response) {
            is RedisMessage.StringMessage
                if response.value == "OK" ->
                JsonSetResult.Ok

            is RedisMessage.NullMessage ->
                JsonSetResult.Aborted

            is RedisMessage.ErrorMessage ->
                handleRedisErrorResponse(
                    response = response,
                )

            else ->
                throw KedisException.WrongResponseException(
                    message = "Expected 'OK' string or null response, was ${response::class.simpleName}",
                )
        }

    override fun toRedisRequest(): RedisMessage =
        RedisMessage.ArrayMessage(
            value = listOf(
                RedisMessage.BulkStringMessage(OPERATION_NAME),
                RedisMessage.BulkStringMessage(key),
                RedisMessage.BulkStringMessage(path),
                RedisMessage.BulkStringMessage(value),
                *options.toRedisMessages()
                    .toTypedArray(),
            ),
        )

    companion object {
        private const val OPERATION_NAME = "JSON.SET"
    }
}
