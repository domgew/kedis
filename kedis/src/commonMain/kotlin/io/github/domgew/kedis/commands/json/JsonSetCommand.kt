package io.github.domgew.kedis.commands.json

import io.github.domgew.kedis.KedisException
import io.github.domgew.kedis.arguments.json.JsonSetOptions
import io.github.domgew.kedis.commands.KedisFullCommand
import io.github.domgew.kedis.impl.RedisMessage
import io.github.domgew.kedis.results.value.SetResult

// see https://redis.io/commands/json.set/
internal class JsonSetCommand(
    val key: String,
    val path: String,
    val value: String,
    val options: JsonSetOptions, // TODO JsonSetOptions
) : KedisFullCommand<SetResult> {
    override fun fromRedisResponse(response: RedisMessage): SetResult =

        when {
            response is RedisMessage.NullMessage ->
                SetResult.NotFound

            response is RedisMessage.StringMessage
                && response.value == "OK" ->
                SetResult.Ok

            response is RedisMessage.ErrorMessage ->
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
                RedisMessage.BulkStringMessage(path),
                RedisMessage.BulkStringMessage(value),
                *options.toRedisMessages()
                    .toTypedArray(),
            ),
        )

    companion object {
        internal const val OPERATION_NAME = "JSON.SET"
    }

}
