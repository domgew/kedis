package io.github.domgew.kedis.commands.value

import io.github.domgew.kedis.KedisException
import io.github.domgew.kedis.arguments.ExpireOptions
import io.github.domgew.kedis.commands.KedisFullCommand
import io.github.domgew.kedis.impl.RedisMessage
import io.github.domgew.kedis.results.value.ExpireTimeResult

internal class ExpireCommand(
    val key: String,
    val seconds: Int,
    val options: ExpireOptions,
) : KedisFullCommand<ExpireTimeResult> {
    override fun fromRedisResponse(response: RedisMessage): ExpireTimeResult =
        when (response) {
            is RedisMessage.IntegerMessage ->
                when (response.value) {
                    -1L ->
                        ExpireTimeResult.Never
                    -2L ->
                        ExpireTimeResult.NotFound
                    else -> ExpireTimeResult.Set
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
                    OPERATION_NAME
                ),
                RedisMessage.BulkStringMessage(key),
                RedisMessage.BulkStringMessage(seconds.toString()),
                *options.toRedisMessages()
                    .toTypedArray(),
            ),
        )

    companion object {
        internal const val OPERATION_NAME = "EXPIRE"
    }
}

