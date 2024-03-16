package io.github.domgew.kedis.commands.value

import io.github.domgew.kedis.KedisException
import io.github.domgew.kedis.commands.KedisFullCommand
import io.github.domgew.kedis.impl.RedisMessage
import io.github.domgew.kedis.results.value.TtlResult

// see https://redis.io/commands/ttl/
// see https://redis.io/commands/pttl/
internal class TtlCommand(
    val key: String,
    val inMilliseconds: Boolean = true,
) : KedisFullCommand<TtlResult> {
    override fun fromRedisResponse(response: RedisMessage): TtlResult =
        when (response) {
            is RedisMessage.IntegerMessage ->
                when (response.value) {
                    -1L ->
                        TtlResult.Never

                    -2L ->
                        TtlResult.NotFound

                    else ->
                        if (inMilliseconds) {
                            TtlResult.InMilliseconds(
                                milliseconds = response.value,
                            )
                        } else {
                            TtlResult.InSeconds(
                                seconds = response.value,
                            )
                        }
                }

            is RedisMessage.ErrorMessage ->
                handleRedisErrorResponse(
                    response = response,
                )

            else ->
                throw KedisException.WrongResponseException(
                    message = "Expected integer response, was ${response::class}",
                )
        }

    override fun toRedisRequest(): RedisMessage =
        RedisMessage.ArrayMessage(
            value = listOf(
                RedisMessage.BulkStringMessage(
                    if (inMilliseconds) {
                        OPERATION_NAME_MILLISECONDS
                    } else {
                        OPERATION_NAME_SECONDS
                    },
                ),
                RedisMessage.BulkStringMessage(key),
            ),
        )

    companion object {
        internal const val OPERATION_NAME_SECONDS = "TTL"
        internal const val OPERATION_NAME_MILLISECONDS = "PTTL"
    }
}
