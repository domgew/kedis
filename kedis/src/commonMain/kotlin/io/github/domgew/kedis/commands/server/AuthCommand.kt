package io.github.domgew.kedis.commands.server

import io.github.domgew.kedis.KedisException
import io.github.domgew.kedis.commands.KedisFullCommand
import io.github.domgew.kedis.impl.RedisMessage

internal class AuthCommand(
    val username: String?,
    val password: String,
): KedisFullCommand<Unit> {
    override fun fromRedisResponse(response: RedisMessage): Unit =
        when {
            response is RedisMessage.StringMessage
                && response.value == "OK" ->
                Unit

            response is RedisMessage.ErrorMessage ->
                handleRedisErrorResponse(
                    response = response,
                )

            else ->
                throw KedisException.WrongResponseException(
                    message = "Expected string or null response, was ${response::class}",
                )
        }

    override fun toRedisRequest(): RedisMessage =
        RedisMessage.ArrayMessage(
            value = listOfNotNull(
                RedisMessage.BulkStringMessage(OPERATION_NAME),
                username
                    ?.let { RedisMessage.BulkStringMessage(it) },
                RedisMessage.BulkStringMessage(password),
            ),
        )

    companion object {
        private const val OPERATION_NAME = "AUTH"
    }
}
