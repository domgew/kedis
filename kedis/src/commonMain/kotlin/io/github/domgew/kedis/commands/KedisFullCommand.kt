package io.github.domgew.kedis.commands

import io.github.domgew.kedis.KedisException
import io.github.domgew.kedis.impl.RedisMessage

internal interface KedisFullCommand<out T> : KedisCommand {
    fun fromRedisResponse(response: RedisMessage): T

    fun handleRedisErrorResponse(response: RedisMessage.ErrorMessage): T {
        throw KedisException.RedisErrorResponseException(response.value)
    }
}
