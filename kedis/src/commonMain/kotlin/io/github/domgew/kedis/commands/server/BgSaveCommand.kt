package io.github.domgew.kedis.commands.server

import io.github.domgew.kedis.KedisException
import io.github.domgew.kedis.commands.KedisFullCommand
import io.github.domgew.kedis.impl.RedisMessage
import io.github.domgew.kedis.results.server.BgSaveResult

// see https://redis.io/commands/bgsave/
internal class BgSaveCommand(
    val schedule: Boolean,
) : KedisFullCommand<BgSaveResult> {
    override fun fromRedisResponse(response: RedisMessage): BgSaveResult =
        when {
            response is RedisMessage.StringMessage
                && response.value == RESULT_SUCCESS_STARTED ->
                BgSaveResult.Started

            response is RedisMessage.StringMessage
                && schedule
                && response.value == RESULT_SUCCESS_SCHEDULED ->
                BgSaveResult.Scheduled

            response is RedisMessage.StringMessage ->
                throw KedisException.WrongResponseException(
                    message = "Expected \"$RESULT_SUCCESS_STARTED\" or \"$RESULT_SUCCESS_SCHEDULED\"," +
                        " was \"${response.value}\"",
                )

            response is RedisMessage.ErrorMessage ->
                handleRedisErrorResponse(
                    response = response,
                )

            else ->
                throw KedisException.WrongResponseException(
                    message = "Expected string response, was ${response::class.simpleName}",
                )
        }

    override fun toRedisRequest(): RedisMessage =
        RedisMessage.ArrayMessage(
            value = listOfNotNull(
                RedisMessage.BulkStringMessage(OPERATION_NAME),
                RedisMessage.BulkStringMessage(ARG_SCHEDULE)
                    .takeIf { schedule },
            ),
        )

    companion object {
        internal const val OPERATION_NAME = "BGSAVE"
        internal const val ARG_SCHEDULE = "SCHEDULE"
        internal const val RESULT_SUCCESS_STARTED = "Background saving started"
        internal const val RESULT_SUCCESS_SCHEDULED = "Background saving scheduled"
    }
}
