package com.github.domgew.kredis.impl

import com.github.domgew.kredis.KredisConfiguration
import com.github.domgew.kredis.KredisException
import com.github.domgew.kredis.arguments.SyncOptions
import com.github.domgew.kredis.commands.KredisStringArrayCommand
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.withLock

internal class DefaultKredisClient(
    configuration: KredisConfiguration,
): AbstractKredisClient(
    configuration = configuration,
) {
    override suspend fun connect() = lock.withLock {
        ensureConnected()
    }

    override suspend fun closeAsync() = lock.withLock {
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
                KredisStringArrayCommand(
                    listOf(
                        "PING",
                        content,
                    ),
                ),
            )
        ) {
            is RedisMessage.StringMessage -> result.value
            is RedisMessage.NullMessage -> null
            else -> throw KredisException.WrongResponse(
                message = "Expected a string response, was ${result::class}"
            )
        }
    }

    override suspend fun serverVersion(): String = lock.withLock {
        return@withLock when (
            val result = executeCommand(
                KredisStringArrayCommand(
                    listOf(
                        "INFO",
                        "server",
                    ),
                ),
            )
        ) {
            is RedisMessage.StringMessage -> result.value
            else -> throw KredisException.WrongResponse(
                message = "Expected a string response, was ${result::class}"
            )
        }
    }

    override suspend fun flushAll(sync: SyncOptions): Boolean = lock.withLock {
        return@withLock when (
            val result = executeCommand(
                KredisStringArrayCommand(
                    listOf(
                        "FLUSHALL",
                        sync.toString(),
                    ),
                ),
            )
        ) {
            is RedisMessage.StringMessage -> result.value == "OK"
            else -> throw KredisException.WrongResponse(
                message = "Expected a string response, was ${result::class}"
            )
        }
    }

    override suspend fun get(key: String): String? = lock.withLock {
        return@withLock when (
            val result = executeCommand(
                KredisStringArrayCommand(
                    listOf(
                        "GET",
                        key,
                    ),
                ),
            )
        ) {
            is RedisMessage.StringMessage -> result.value
            is RedisMessage.NullMessage -> null
            else -> throw KredisException.WrongResponse(
                message = "Expected a string response, was ${result::class}"
            )
        }
    }

    override suspend fun set(key: String, value: String): String? = lock.withLock {
        return@withLock when (
            val result = executeCommand(
                KredisStringArrayCommand(
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
            else -> throw KredisException.WrongResponse(
                message = "Expected a string response, was ${result::class}"
            )
        }
    }
}
