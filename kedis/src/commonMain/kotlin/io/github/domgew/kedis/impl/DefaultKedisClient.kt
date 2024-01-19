package io.github.domgew.kedis.impl

import io.github.domgew.kedis.KedisConfiguration
import io.github.domgew.kedis.arguments.InfoSectionName
import io.github.domgew.kedis.arguments.SetOptions
import io.github.domgew.kedis.arguments.SyncOption
import io.github.domgew.kedis.commands.server.FlushCommand
import io.github.domgew.kedis.commands.value.GetCommand
import io.github.domgew.kedis.commands.server.InfoCommand
import io.github.domgew.kedis.commands.server.InfoMapCommand
import io.github.domgew.kedis.commands.server.InfoRawCommand
import io.github.domgew.kedis.commands.server.PingCommand
import io.github.domgew.kedis.commands.server.WhoAmICommand
import io.github.domgew.kedis.commands.value.DelCommand
import io.github.domgew.kedis.commands.value.ExistsCommand
import io.github.domgew.kedis.commands.value.GetBinaryCommand
import io.github.domgew.kedis.commands.value.SetBinaryCommand
import io.github.domgew.kedis.commands.value.SetCommand
import io.github.domgew.kedis.results.server.InfoSection
import io.github.domgew.kedis.results.value.SetBinaryResult
import io.github.domgew.kedis.results.value.SetResult
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

internal class DefaultKedisClient(
    configuration: KedisConfiguration,
): AbstractKedisClient(
    configuration = configuration,
) {
    private val lock = Mutex()

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

    override suspend fun ping(
        content: String,
    ): String = lock.withLock {
        executeCommand(
            PingCommand(
                content = content,
            ),
        )
    }

    override suspend fun auth(
        password: String,
        username: String?,
    ) = lock.withLock {
        performAuthentication(
            username = username,
            password = password,
        )
    }

    override suspend fun whoAmI(): String = lock.withLock {
        executeCommand(
            WhoAmICommand(),
        )
    }

    override suspend fun info(
        vararg section: InfoSectionName,
    ): List<InfoSection> = lock.withLock {
        executeCommand(
            InfoCommand(
                sections = section.asList(),
            ),
        )
    }

    override suspend fun infoMap(
        vararg section: InfoSectionName,
    ): Map<String?, Map<String, String>> = lock.withLock {
        executeCommand(
            InfoMapCommand(
                sections = section.asList(),
            ),
        )
    }

    override suspend fun infoRaw(
        vararg section: InfoSectionName,
    ): String? = lock.withLock {
        executeCommand(
            InfoRawCommand(
                sections = section.asList(),
            ),
        )
    }

    override suspend fun flushAll(
        sync: SyncOption,
    ): Boolean = lock.withLock {
        executeCommand(
            FlushCommand(
                target = FlushCommand.FlushTarget.ALL,
                syncOption = sync,
            ),
        )
    }

    override suspend fun flushDb(
        sync: SyncOption,
    ): Boolean = lock.withLock {
        executeCommand(
            FlushCommand(
                target = FlushCommand.FlushTarget.DB,
                syncOption = sync,
            ),
        )
    }

    override suspend fun get(
        key: String,
    ): String? = lock.withLock {
        executeCommand(
            GetCommand(
                key = key,
            ),
        )
    }

    override suspend fun getBinary(
        key: String,
    ): ByteArray? = lock.withLock {
        executeCommand(
            GetBinaryCommand(
                key = key,
            ),
        )
    }

    override suspend fun set(
        key: String,
        value: String,
        options: SetOptions,
    ): SetResult = lock.withLock {
        executeCommand(
            SetCommand(
                key = key,
                value = value,
                options = options,
            ),
        )
    }

    override suspend fun setBinary(
        key: String,
        value: ByteArray,
        options: SetOptions,
    ): SetBinaryResult = lock.withLock {
        executeCommand(
            SetBinaryCommand(
                key = key,
                value = value,
                options = options,
            ),
        )
    }

    override suspend fun del(
        vararg key: String,
    ): Long = lock.withLock {
        executeCommand(
            DelCommand(
                keys = key.asList()
                    // when no keys, the response is clear even without the server
                    .takeUnless { it.isEmpty() }
                    ?: return@withLock 0,
            ),
        )
    }

    override suspend fun exists(
        vararg key: String,
    ): Long = lock.withLock {
        executeCommand(
            ExistsCommand(
                keys = key.asList()
                    // when no keys, the response is clear even without the server
                    .takeUnless { it.isEmpty() }
                    ?: return@withLock 0,
            ),
        )
    }
}
