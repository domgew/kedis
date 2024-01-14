package io.github.domgew.kedis.commands.server

import io.github.domgew.kedis.arguments.SyncOption
import io.github.domgew.kedis.commands.KedisFullCommand
import io.github.domgew.kedis.impl.RedisMessage

internal class FlushCommand(
    val target: FlushTarget,
    val syncOption: SyncOption,
) : KedisFullCommand<Boolean> {
    override fun fromRedisResponse(response: RedisMessage): Boolean =
        when {
            response is RedisMessage.StringMessage
                && response.value == "OK" ->
                true

            else ->
                false
        }

    override fun toRedisRequest(): RedisMessage =
        RedisMessage.ArrayMessage(
            value = listOf(
                RedisMessage.BulkStringMessage(target.toString()),
                RedisMessage.BulkStringMessage(syncOption.toString()),
            ),
        )

    internal enum class FlushTarget(
        private val operationName: String,
    ) {
        DB("FLUSHDB"),
        ALL("FLUSHALL"),
        ;

        override fun toString(): String =
            operationName
    }
}
