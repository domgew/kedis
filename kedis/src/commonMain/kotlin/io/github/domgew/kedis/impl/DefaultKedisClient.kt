package io.github.domgew.kedis.impl

import io.github.domgew.kedis.KedisConfiguration
import io.github.domgew.kedis.KedisException
import io.github.domgew.kedis.arguments.SetOptions
import io.github.domgew.kedis.arguments.SyncOption
import io.github.domgew.kedis.commands.KedisMessageArrayCommand
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
                message = "Expected a string response, was ${result::class}",
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
                message = "Expected a string response, was ${result::class}",
            )
        }
    }

    override suspend fun flushAll(sync: SyncOption): Boolean = lock.withLock {
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
                message = "Expected a string response, was ${result::class}",
            )
        }
    }

    override suspend fun flushDb(sync: SyncOption): Boolean = lock.withLock {
        return@withLock when (
            val result = executeCommand(
                KedisStringArrayCommand(
                    listOf(
                        "FLUSHDB",
                        sync.toString(),
                    ),
                ),
            )
        ) {
            is RedisMessage.StringMessage -> result.value == "OK"

            else -> throw KedisException.WrongResponse(
                message = "Expected a string response, was ${result::class}",
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
                message = "Expected a string response, was ${result::class}",
            )
        }
    }

    override suspend fun set(
        key: String,
        value: String,
        options: SetOptions,
    ): String? = lock.withLock {
        return@withLock when (
            val result = executeCommand(
                KedisMessageArrayCommand(
                    listOf(
                        RedisMessage.BulkStringMessage("SET"),
                        RedisMessage.BulkStringMessage(key),
                        RedisMessage.BulkStringMessage(value),
                        *options.toRedisMessages().toTypedArray(),
                    ),
                ),
            )
        ) {
            is RedisMessage.StringMessage -> {
                if (!options.getPreviousValue && result.value != "OK") {
                    throw KedisException.WrongResponse(
                        message = "Expected OK response, was \"${result.value}\"",
                    )
                } else {
                    result.value
                }
            }

            is RedisMessage.NullMessage -> null

            else -> throw KedisException.WrongResponse(
                message = "Expected a string response, was ${result::class}",
            )
        }
    }

    override suspend fun del(vararg key: String): Long = lock.withLock {
        return@withLock when (
            val result = executeCommand(
                KedisStringArrayCommand(
                    listOf(
                        "DEL",
                        *key,
                    ),
                ),
            )
        ) {
            is RedisMessage.IntegerMessage -> result.value

            else -> throw KedisException.WrongResponse(
                message = "Expected a string response, was ${result::class}",
            )
        }
    }

    override suspend fun exists(vararg key: String): Long = lock.withLock {
        return@withLock when (
            val result = executeCommand(
                KedisStringArrayCommand(
                    listOf(
                        "EXISTS",
                        *key,
                    ),
                ),
            )
        ) {
            is RedisMessage.IntegerMessage -> result.value

            else -> throw KedisException.WrongResponse(
                message = "Expected a string response, was ${result::class}",
            )
        }
    }
}
