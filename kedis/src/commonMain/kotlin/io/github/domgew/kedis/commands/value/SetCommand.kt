package io.github.domgew.kedis.commands.value

import io.github.domgew.kedis.KedisException
import io.github.domgew.kedis.arguments.SetOptions
import io.github.domgew.kedis.commands.KedisFullCommand
import io.github.domgew.kedis.impl.RedisMessage
import io.github.domgew.kedis.results.value.SetResult

// see https://redis.io/commands/set/
internal class SetCommand(
    val key: String,
    val value: String,
    val options: SetOptions,
): KedisFullCommand<SetResult> {
    override fun fromRedisResponse(response: RedisMessage): SetResult =
        when {
            !options.getPreviousValue
                && response is RedisMessage.NullMessage ->
                SetResult.Aborted

            !options.getPreviousValue
                && response is RedisMessage.StringMessage
                && response.value == "OK" ->
                SetResult.Ok

            options.getPreviousValue
                && response is RedisMessage.NullMessage ->
                SetResult.NotFound

            options.getPreviousValue
                && response is RedisMessage.StringMessage ->
                SetResult.PreviousValue(
                    data = response.value,
                )

            response is RedisMessage.StringMessage ->
                throw KedisException.WrongResponse(
                    message = "Expected \"OK\" or data, was \"${response.value}\"",
                )

            else ->
                throw KedisException.WrongResponse(
                    message = "Expected string response, was ${response::class}",
                )
        }

    override fun toRedisRequest(): RedisMessage =
        RedisMessage.ArrayMessage(
            value = listOf(
                RedisMessage.BulkStringMessage(OPERATION_NAME),
                RedisMessage.BulkStringMessage(key),
                RedisMessage.BulkStringMessage(value),
                *options.toRedisMessages().toTypedArray(),
            ),
        )

    companion object {
        internal const val OPERATION_NAME = "SET"
    }
}
