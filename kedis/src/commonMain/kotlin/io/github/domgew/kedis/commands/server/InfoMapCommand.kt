package io.github.domgew.kedis.commands.server

import io.github.domgew.kedis.KedisException
import io.github.domgew.kedis.arguments.InfoSectionName
import io.github.domgew.kedis.commands.KedisFullCommand
import io.github.domgew.kedis.impl.RedisMessage
import io.github.domgew.kedis.results.server.InfoSection

internal class InfoMapCommand(
    val sections: List<InfoSectionName>,
) : KedisFullCommand<Map<String?, Map<String, String>>> {
    override fun fromRedisResponse(response: RedisMessage): Map<String?, Map<String, String>> =
        when (response) {
            is RedisMessage.StringMessage ->
                InfoSection.parseMap(
                    response = response.value,
                )

            is RedisMessage.NullMessage ->
                emptyMap()

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
