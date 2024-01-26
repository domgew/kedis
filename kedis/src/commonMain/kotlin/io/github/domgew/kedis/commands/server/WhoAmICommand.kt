package io.github.domgew.kedis.commands.server

import io.github.domgew.kedis.KedisException
import io.github.domgew.kedis.commands.KedisFullCommand
import io.github.domgew.kedis.impl.RedisMessage

internal class WhoAmICommand : KedisFullCommand<String> {
    override fun fromRedisResponse(response: RedisMessage): String =
        when (response) {
            is RedisMessage.StringMessage ->
                response.value

            is RedisMessage.ErrorMessage ->
                handleRedisErrorResponse(
                    response = response,
                )

            else ->
                throw KedisException.WrongResponseException(
                    message = "Expected string response, was ${response::class}",
                )
        }

    override fun toRedisRequest(): RedisMessage =
        RedisMessage.ArrayMessage(
            value = listOfNotNull(
                RedisMessage.BulkStringMessage(OPERATION_GROUP_NAME),
                RedisMessage.BulkStringMessage(OPERATION_NAME),
            ),
        )

    companion object {
        private const val OPERATION_GROUP_NAME = "ACL"
        private const val OPERATION_NAME = "WHOAMI"
    }
}
