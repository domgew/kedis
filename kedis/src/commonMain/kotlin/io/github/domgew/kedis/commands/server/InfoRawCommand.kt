package io.github.domgew.kedis.commands.server

import io.github.domgew.kedis.KedisException
import io.github.domgew.kedis.arguments.InfoSectionName
import io.github.domgew.kedis.commands.KedisFullCommand
import io.github.domgew.kedis.impl.RedisMessage

internal class InfoRawCommand(
    val sections: List<InfoSectionName>,
) : KedisFullCommand<String?> {
    override fun fromRedisResponse(response: RedisMessage): String? =
        when (response) {
            is RedisMessage.StringMessage ->
                response.value

            is RedisMessage.NullMessage ->
                null

            else ->
                throw KedisException.WrongResponseException(
                    message = "Expected string response, was ${response::class}",
                )
        }

    override fun toRedisRequest(): RedisMessage =
        RedisMessage.ArrayMessage(
            value = listOf(
                RedisMessage.BulkStringMessage(OPERATION_NAME),
                *sections
                    .map { RedisMessage.BulkStringMessage(it.toString()) }
                    .toTypedArray(),
            ),
        )

    companion object {
        private const val OPERATION_NAME = "INFO"
    }
}