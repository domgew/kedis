package io.github.domgew.kedis.commands.json

import io.github.domgew.kedis.KedisException
import io.github.domgew.kedis.arguments.json.JsonGetOptions
import io.github.domgew.kedis.commands.KedisFullCommand
import io.github.domgew.kedis.impl.RedisMessage

// https://redis.io/commands/json.get/
internal class JsonGetCommand(
    val key: String,
    val path: String,
    val options: JsonGetOptions,
) : KedisFullCommand<String?> {
    override fun fromRedisResponse(response: RedisMessage): String? =
        when (response) {
            is RedisMessage.StringMessage ->
                response.value

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
                RedisMessage.BulkStringMessage(path),
                *options.toRedisMessages()
                    .toTypedArray(),
            ),
        )

    companion object {
        private const val OPERATION_NAME = "JSON.GET"
    }
}
