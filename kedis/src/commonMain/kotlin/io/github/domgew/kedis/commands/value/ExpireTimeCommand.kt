package io.github.domgew.kedis.commands.value

import io.github.domgew.kedis.KedisException
import io.github.domgew.kedis.commands.KedisFullCommand
import io.github.domgew.kedis.impl.RedisMessage
import io.github.domgew.kedis.results.value.ExpireTimeResult

// see https://redis.io/commands/expiretime/
// see https://redis.io/commands/pexpiretime/
internal class ExpireTimeCommand(
    val key: String,
    val inMilliseconds: Boolean = true,
) : KedisFullCommand<ExpireTimeResult> {
    override fun fromRedisResponse(response: RedisMessage): ExpireTimeResult =
        when (response) {
            is RedisMessage.IntegerMessage ->
                when (response.value) {
                    -1L ->
                        ExpireTimeResult.Never

                    -2L ->
                        ExpireTimeResult.NotFound

                    else ->
                        if (inMilliseconds) {
                            ExpireTimeResult.AtUnixMillisecond(
                                milliseconds = response.value,
                            )
                        } else {
                            ExpireTimeResult.AtUnixSecond(
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
                    message = "Expected integer response, was ${response::class.simpleName}",
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
        internal const val OPERATION_NAME_SECONDS = "EXPIRETIME"
        internal const val OPERATION_NAME_MILLISECONDS = "PEXPIRETIME"
    }
}
