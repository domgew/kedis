package io.github.domgew.kedis.impl

import io.github.domgew.kedis.KedisConfiguration
import io.github.domgew.kedis.KedisException
import io.github.domgew.kedis.arguments.SyncOptions
import io.github.domgew.kedis.commands.KedisStringArrayCommand
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.withLock

internal class DefaultKedisClient(
    configuration: KedisConfiguration,
): AbstractKedisClient(
    configuration = configuration,
) {
    override suspend fun connect() = lock.withLock {
        ensureConnected()
    }

    override suspend fun closeSuspended() = lock.withLock {
        doClose()
    }

    override fun close() = runBlocking {
        lock.withLock {
            doClose()
        }
    }

    override suspend fun ping(content: String): String? = lock.withLock {
        return@withLock when (
            val result = executeCommand(
                KedisStringArrayCommand(
                    listOf(
                        "PING",
                        content,
                    ),
                ),
            )
        ) {
            is RedisMessage.StringMessage -> result.value
            is RedisMessage.NullMessage -> null
            else -> throw KedisException.WrongResponse(
                message = "Expected a string response, was ${result::class}"
            )
        }
    }

    override suspend fun serverVersion(): String = lock.withLock {
        return@withLock when (
            val result = executeCommand(
                KedisStringArrayCommand(
                    listOf(
                        "INFO",
                        "server",
                    ),
                ),
            )
        ) {
            is RedisMessage.StringMessage -> result.value
            else -> throw KedisException.WrongResponse(
                message = "Expected a string response, was ${result::class}"
            )
        }
    }

    override suspend fun flushAll(sync: SyncOptions): Boolean = lock.withLock {
        return@withLock when (
            val result = executeCommand(
                KedisStringArrayCommand(
                    listOf(
                        "FLUSHALL",
                        sync.toString(),
                    ),
                ),
            )
        ) {
            is RedisMessage.StringMessage -> result.value == "OK"
            else -> throw KedisException.WrongResponse(
                message = "Expected a string response, was ${result::class}"
            )
        }
    }

    override suspend fun get(key: String): String? = lock.withLock {
        return@withLock when (
            val result = executeCommand(
                KedisStringArrayCommand(
                    listOf(
                        "GET",
                        key,
                    ),
                ),
            )
        ) {
            is RedisMessage.StringMessage -> result.value
            is RedisMessage.NullMessage -> null
            else -> throw KedisException.WrongResponse(
                message = "Expected a string response, was ${result::class}"
            )
        }
    }

    override suspend fun set(key: String, value: String): String? = lock.withLock {
        return@withLock when (
            val result = executeCommand(
                KedisStringArrayCommand(
                    listOf(
                        "SET",
                        key,
                        value,
                    ),
                ),
            )
        ) {
            is RedisMessage.StringMessage -> result.value
            is RedisMessage.NullMessage -> null
            else -> throw KedisException.WrongResponse(
                message = "Expected a string response, was ${result::class}"
            )
        }
    }
}
