package io.github.domgew.kedis.commands.scripting

import io.github.domgew.kedis.commands.KedisFullCommand
import io.github.domgew.kedis.impl.RedisMessage
import io.github.domgew.kedis.results.scripting.DynamicResult

// // see https://redis.io/commands/eval/
internal class EvalCommand(
    val script: String,
    val keys: List<String>,
    val args: List<String>,
) : KedisFullCommand<DynamicResult> {

    override fun fromRedisResponse(
        response: RedisMessage,
    ): DynamicResult =
        DynamicResult.fromMessage(
            message = response,
        )

    override fun toRedisRequest(): RedisMessage =
        RedisMessage.ArrayMessage(
            value = listOf(
                RedisMessage.BulkStringMessage(OPERATION_NAME),
                RedisMessage.BulkStringMessage(script),
                RedisMessage.BulkStringMessage(keys.size.toString()),
                *keys
                    .map {
                        RedisMessage.BulkStringMessage(it)
                    }
                    .toTypedArray(),
                *args
                    .map {
                        RedisMessage.BulkStringMessage(it)
                    }
                    .toTypedArray(),
            ),
        )

    companion object {

        private const val OPERATION_NAME = "EVAL"
    }
}
