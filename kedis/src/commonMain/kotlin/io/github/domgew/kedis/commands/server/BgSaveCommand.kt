package io.github.domgew.kedis.commands.server

import io.github.domgew.kedis.KedisException
import io.github.domgew.kedis.commands.KedisFullCommand
import io.github.domgew.kedis.impl.RedisMessage

// see https://redis.io/commands/bgsave/
// TODO: add tests
internal class BgSaveCommand(
    val schedule: Boolean,
) : KedisFullCommand<Unit> {
    override fun fromRedisResponse(response: RedisMessage): Unit =
        when {
            response is RedisMessage.StringMessage
                && !schedule
                && response.value == RESULT_SUCCESS_STARTED ->
                Unit

            response is RedisMessage.StringMessage
                && schedule
                && response.value == RESULT_SUCCESS_SCHEDULED ->
                Unit

            response is RedisMessage.StringMessage ->
                throw KedisException.WrongResponseException(
                    message = "Expected \"${
                        if (!schedule)
                            RESULT_SUCCESS_STARTED
                        else
                            RESULT_SUCCESS_SCHEDULED
                    }\", was \"${response.value}\"",
                )

            response is RedisMessage.ErrorMessage ->
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
                RedisMessage.BulkStringMessage(OPERATION_NAME),
                RedisMessage.BulkStringMessage(ARG_SCHEDULE)
                    .takeIf { schedule },
            ),
        )

    companion object {
        internal const val OPERATION_NAME = "APPEND"
        internal const val ARG_SCHEDULE = "SCHEDULE"
        internal const val RESULT_SUCCESS_STARTED = "Background saving started"
        internal const val RESULT_SUCCESS_SCHEDULED = "Background saving scheduled"
    }
}
