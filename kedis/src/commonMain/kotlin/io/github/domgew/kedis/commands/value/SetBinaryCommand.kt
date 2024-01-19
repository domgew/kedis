package io.github.domgew.kedis.commands.value

import io.github.domgew.kedis.KedisException
import io.github.domgew.kedis.arguments.SetOptions
import io.github.domgew.kedis.commands.KedisFullCommand
import io.github.domgew.kedis.impl.RedisMessage
import io.github.domgew.kedis.results.value.SetBinaryResult

// see https://redis.io/commands/set/
internal class SetBinaryCommand(
    val key: String,
    val value: ByteArray,
    val options: SetOptions,
): KedisFullCommand<SetBinaryResult> {
    override fun fromRedisResponse(response: RedisMessage): SetBinaryResult =
        when {
            !options.getPreviousValue
                && response is RedisMessage.NullMessage ->
                SetBinaryResult.Aborted

            !options.getPreviousValue
                && response is RedisMessage.StringMessage
                && response.value == "OK" ->
                SetBinaryResult.Ok

            options.getPreviousValue
                && response is RedisMessage.NullMessage ->
                SetBinaryResult.NotFound

            options.getPreviousValue
                && response is RedisMessage.BulkStringMessage ->
                SetBinaryResult.PreviousValue(
                    data = response.data,
                )

            options.getPreviousValue
                && response is RedisMessage.StringMessage ->
                SetBinaryResult.PreviousValue(
                    data = response.value.encodeToByteArray(),
                )

            response is RedisMessage.StringMessage ->
                throw KedisException.WrongResponse(
                    message = "Expected \"OK\" or data, was \"${response.value}\"",
                )

            response is RedisMessage.ErrorMessage ->
                handleRedisErrorResponse(
                    response = response,
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
