package io.github.domgew.kedis.impl

import io.github.domgew.kedis.KedisConfiguration
import io.github.domgew.kedis.arguments.InfoSectionName
import io.github.domgew.kedis.arguments.SetOptions
import io.github.domgew.kedis.arguments.SyncOption
import io.github.domgew.kedis.commands.hash.HashDelCommand
import io.github.domgew.kedis.commands.hash.HashExistsCommand
import io.github.domgew.kedis.commands.hash.HashGetAllBinaryCommand
import io.github.domgew.kedis.commands.hash.HashGetAllCommand
import io.github.domgew.kedis.commands.hash.HashGetBinaryCommand
import io.github.domgew.kedis.commands.hash.HashGetCommand
import io.github.domgew.kedis.commands.hash.HashKeysCommand
import io.github.domgew.kedis.commands.hash.HashLengthCommand
import io.github.domgew.kedis.commands.hash.HashSetBinaryCommand
import io.github.domgew.kedis.commands.hash.HashSetCommand
import io.github.domgew.kedis.commands.server.BgSaveCommand
import io.github.domgew.kedis.commands.server.FlushCommand
import io.github.domgew.kedis.commands.server.InfoCommand
import io.github.domgew.kedis.commands.server.InfoMapCommand
import io.github.domgew.kedis.commands.server.InfoRawCommand
import io.github.domgew.kedis.commands.server.PingCommand
import io.github.domgew.kedis.commands.server.WhoAmICommand
import io.github.domgew.kedis.commands.value.AppendCommand
import io.github.domgew.kedis.commands.value.DecrByCommand
import io.github.domgew.kedis.commands.value.DecrCommand
import io.github.domgew.kedis.commands.value.DelCommand
import io.github.domgew.kedis.commands.value.ExistsCommand
import io.github.domgew.kedis.commands.value.ExpireTimeCommand
import io.github.domgew.kedis.commands.value.GetBinaryCommand
import io.github.domgew.kedis.commands.value.GetCommand
import io.github.domgew.kedis.commands.value.GetRangeCommand
import io.github.domgew.kedis.commands.value.IncrByCommand
import io.github.domgew.kedis.commands.value.IncrByFloatCommand
import io.github.domgew.kedis.commands.value.IncrCommand
import io.github.domgew.kedis.commands.value.SetBinaryCommand
import io.github.domgew.kedis.commands.value.SetCommand
import io.github.domgew.kedis.commands.value.StrLenCommand
import io.github.domgew.kedis.commands.value.TtlCommand
import io.github.domgew.kedis.results.server.InfoSection
import io.github.domgew.kedis.results.value.ExpireTimeResult
import io.github.domgew.kedis.results.value.SetBinaryResult
import io.github.domgew.kedis.results.value.SetResult
import io.github.domgew.kedis.results.value.TtlResult
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

internal class DefaultKedisClient(
    configuration: KedisConfiguration,
) : AbstractKedisClient(
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

    override suspend fun bgSave(
        schedule: Boolean,
    ) = lock.withLock {
        executeCommand(
            BgSaveCommand(
                schedule = schedule,
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

    override suspend fun getRange(
        key: String,
        start: Long,
        end: Long
    ): String = lock.withLock {
        executeCommand(
            GetRangeCommand(
                key = key,
                start = start,
                end = end,
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

    override suspend fun expireTime(
        key: String,
        inMilliseconds: Boolean,
    ): ExpireTimeResult = lock.withLock {
        executeCommand(
            ExpireTimeCommand(
                key = key,
                inMilliseconds = inMilliseconds,
            ),
        )
    }

    override suspend fun ttl(
        key: String,
        inMilliseconds: Boolean,
    ): TtlResult = lock.withLock {
        executeCommand(
            TtlCommand(
                key = key,
                inMilliseconds = inMilliseconds,
            ),
        )
    }

    override suspend fun append(
        key: String,
        value: String,
    ): Long = lock.withLock {
        executeCommand(
            AppendCommand(
                key = key,
                value = value,
            ),
        )
    }

    override suspend fun strLen(
        key: String,
    ): Long = lock.withLock {
        executeCommand(
            StrLenCommand(
                key = key,
            ),
        )
    }

    override suspend fun decr(
        key: String,
    ): Long = lock.withLock {
        executeCommand(
            DecrCommand(
                key = key,
            ),
        )
    }

    override suspend fun decrBy(
        key: String,
        by: Long,
    ): Long = lock.withLock {
        executeCommand(
            DecrByCommand(
                key = key,
                by = by,
            ),
        )
    }

    override suspend fun incr(
        key: String,
    ): Long = lock.withLock {
        executeCommand(
            IncrCommand(
                key = key,
            ),
        )
    }

    override suspend fun incrBy(
        key: String,
        by: Long,
    ): Long = lock.withLock {
        executeCommand(
            IncrByCommand(
                key = key,
                by = by,
            ),
        )
    }

    override suspend fun incrByFloat(
        key: String,
        by: Double,
    ): Double = lock.withLock {
        executeCommand(
            IncrByFloatCommand(
                key = key,
                by = by,
            ),
        )
    }

    override suspend fun hashGet(
        key: String,
        field: String,
    ): String? = lock.withLock {
        executeCommand(
            HashGetCommand(
                key = key,
                field = field,
            ),
        )
    }

    override suspend fun hashGetBinary(
        key: String,
        field: String,
    ): ByteArray? = lock.withLock {
        executeCommand(
            HashGetBinaryCommand(
                key = key,
                field = field,
            ),
        )
    }

    override suspend fun hashGetAll(
        key: String,
    ): Map<String, String>? = lock.withLock {
        executeCommand(
            HashGetAllCommand(
                key = key,
            ),
        )
    }

    override suspend fun hashGetAllBinary(
        key: String,
    ): Map<String, ByteArray>? = lock.withLock {
        executeCommand(
            HashGetAllBinaryCommand(
                key = key,
            ),
        )
    }

    override suspend fun hashSet(
        key: String,
        fieldValues: Map<String, String>,
    ): Long = lock.withLock {
        executeCommand(
            HashSetCommand(
                key = key,
                fieldValues = fieldValues,
            ),
        )
    }

    override suspend fun hashSetBinary(
        key: String,
        fieldValues: Map<String, ByteArray>,
    ): Long = lock.withLock {
        executeCommand(
            HashSetBinaryCommand(
                key = key,
                fieldValues = fieldValues,
            ),
        )
    }

    override suspend fun hashDel(
        key: String,
        vararg field: String,
    ): Long = lock.withLock {
        executeCommand(
            HashDelCommand(
                key = key,
                fields = field.asList(),
            ),
        )
    }

    override suspend fun hashExists(
        key: String,
        field: String,
    ): Boolean = lock.withLock {
        executeCommand(
            HashExistsCommand(
                key = key,
                field = field,
            ),
        )
    }

    override suspend fun hashKeys(
        key: String,
    ): List<String>? = lock.withLock {
        executeCommand(
            HashKeysCommand(
                key = key,
            ),
        )
    }

    override suspend fun hashLength(
        key: String,
    ): Long = lock.withLock {
        executeCommand(
            HashLengthCommand(
                key = key,
            ),
        )
    }
}
