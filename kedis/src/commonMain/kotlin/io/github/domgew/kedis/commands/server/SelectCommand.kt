package io.github.domgew.kedis.commands.server

import io.github.domgew.kedis.KedisException
import io.github.domgew.kedis.commands.KedisFullCommand
import io.github.domgew.kedis.impl.RedisMessage

internal class SelectCommand(private val index: Int) : KedisFullCommand<Unit> {

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
                    message = "Expected string or null response, was ${response::class.simpleName}",
                )
        }

    override fun toRedisRequest(): RedisMessage =
        RedisMessage.ArrayMessage(
            value = listOfNotNull(
                RedisMessage.BulkStringMessage(OPERATION_NAME),
                RedisMessage.BulkStringMessage(index.toString()),
            ),
        )

    companion object {
        private const val OPERATION_NAME = "SELECT"
    }

}
