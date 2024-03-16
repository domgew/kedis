package io.github.domgew.kedis.commands.server

import io.github.domgew.kedis.KedisException
import io.github.domgew.kedis.arguments.InfoSectionName
import io.github.domgew.kedis.commands.KedisFullCommand
import io.github.domgew.kedis.impl.RedisMessage
import io.github.domgew.kedis.results.server.InfoSection

internal class InfoCommand(
    val sections: List<InfoSectionName>,
) : KedisFullCommand<List<InfoSection>> {
    override fun fromRedisResponse(response: RedisMessage): List<InfoSection> =
        when (response) {
            is RedisMessage.StringMessage ->
                InfoSection.parse(
                    responseMap = InfoSection.parseMap(
                        response = response.value,
                    ),
                )

            is RedisMessage.NullMessage ->
                emptyList()

            else ->
                throw KedisException.WrongResponseException(
                    message = "Expected string response, was ${response::class.simpleName}",
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
