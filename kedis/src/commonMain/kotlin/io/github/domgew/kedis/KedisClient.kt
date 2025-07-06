package io.github.domgew.kedis

import io.github.domgew.kedis.KedisClient.Companion.invoke
import io.github.domgew.kedis.KedisClient.Companion.newClient
import io.github.domgew.kedis.arguments.server.InfoSectionName
import io.github.domgew.kedis.arguments.server.SyncOption
import io.github.domgew.kedis.arguments.value.SetOptions
import io.github.domgew.kedis.commands.KedisCommand
import io.github.domgew.kedis.commands.KedisHashCommands
import io.github.domgew.kedis.commands.KedisServerCommands
import io.github.domgew.kedis.commands.KedisValueCommands
import io.github.domgew.kedis.impl.DefaultKedisClient
import io.github.domgew.kedis.results.server.BgSaveResult
import io.github.domgew.kedis.results.server.InfoSection
import io.github.domgew.kedis.results.value.ExpireTimeResult
import io.github.domgew.kedis.results.value.SetBinaryResult
import io.github.domgew.kedis.results.value.SetResult
import io.github.domgew.kedis.results.value.TtlResult
import io.ktor.network.selector.SelectorManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO

/**
 * The public interface of the client.
 * Use [KedisClient.newClient] or [KedisClient.invoke] to create an instance.
 * See [io.github.domgew.kedis.commands] for available commands.
 *
 * @see KedisClient.newClient
 * @see KedisClient.invoke
 * @see io.github.domgew.kedis.commands
 */
public interface KedisClient : AutoCloseable {

    public companion object {

        /**
         * Creates a new client instance without connecting.
         *
         * When you connect the client, make sure to disconnect/[close] it again.
         * Each command (method) will connect automatically, when the connection is not already open.
         *
         * @see invoke
         */
        public fun newClient(
            configuration: KedisConfiguration,
        ): KedisClient =
            newClient(
                configuration = configuration,
                selectorManager = SelectorManager(
                    Dispatchers.IO,
                ),
            )

        /**
         * Creates a new client instance without connecting.
         *
         * When you connect the client, make sure to disconnect/[close] it again.
         * Each command (method) will connect automatically, when the connection is not already open.
         *
         * @see newClient
         */
        public operator fun invoke(
            configuration: KedisConfiguration,
        ): KedisClient =
            newClient(
                configuration = configuration,
            )

        internal fun newClient(
            configuration: KedisConfiguration,
            selectorManager: SelectorManager,
        ) =
            DefaultKedisClient(
                configuration = configuration,
                selectorManager = selectorManager,
            )
    }

    /**
     * Checks whether the client has a connection to the server and the connection reports to be active.
     */
    public val isConnected: Boolean

    /**
     * Checks whether the client has a connection to the server and the connection reports to be active.
     */
    public val probablyConnected: Boolean

    /**
     * The currently selected database. It is the client's best estimate.
     *
     * Only valid while connected and no select command is currently being executed.
     *
     * @see KedisConfiguration.databaseIndex
     * @see KedisServerCommands.select
     */
    public val selectedDatabase: Int

    /**
     * Manually ensures that the client is connected. When [isConnected] is true, nothing happens, otherwise the connection is established.
     */
    public suspend fun connect()

    /**
     * Executes the given [command] and returns its result.
     * For available commands see [io.github.domgew.kedis.commands].
     *
     * @return The command's result
     * @see [io.github.domgew.kedis.commands]
     */
    public suspend fun <T> execute(
        command: KedisCommand<T>,
    ): T

    /**
     * Creates a [KedisPipelineClient] that uses the same underlying connection.
     *
     * It does not block this client until [KedisPipelineClient.execute] is called.
     *
     * [https://redis.io/docs/latest/develop/use/pipelining/](https://redis.io/docs/latest/develop/use/pipelining/)
     * @return The pipelined client
     * @see KedisPipelineClient
     * @sample io.github.domgew.kedis.samples.PipelineSamples.simple
     */
    public fun pipelined(): KedisPipelineClient

    /**
     * Closes the connection to the server.
     */
    @Deprecated(
        message = "Not suspending anymore",
        replaceWith = ReplaceWith(
            expression = "close()",
        ),
    )
    public suspend fun closeSuspended() {
        close()
    }

    /**
     * @see [KedisServerCommands.ping]
     */
    @Deprecated(
        message = "Use commands",
        replaceWith = ReplaceWith(
            expression = "execute(\n" +
                "    command = KedisServerCommands.ping(\n" +
                "        content = content,\n" +
                "    ),\n" +
                ")",
            "io.github.domgew.kedis.commands.KedisServerCommands",
        ),
    )
    public suspend fun ping(
        content: String = "PING",
    ): String =
        execute(
            command = KedisServerCommands.ping(
                content = content,
            ),
        )

    /**
     * @see [KedisServerCommands.auth]
     */
    @Deprecated(
        message = "Use commands",
        replaceWith = ReplaceWith(
            expression = "execute(\n" +
                "    command = KedisServerCommands.auth(\n" +
                "        password = password,\n" +
                "        username = username,\n" +
                "    ),\n" +
                ")",
            "io.github.domgew.kedis.commands.KedisServerCommands",
        ),
    )
    public suspend fun auth(
        password: String,
        username: String? = null,
    ): Unit =
        execute(
            command = KedisServerCommands.auth(
                password = password,
                username = username,
            ),
        )

    /**
     * @see [KedisServerCommands.whoAmI]
     */
    @Deprecated(
        message = "Use commands",
        replaceWith = ReplaceWith(
            expression = "execute(\n" +
                "    command = KedisServerCommands.whoAmI(),\n" +
                ")",
            "io.github.domgew.kedis.commands.KedisServerCommands",
        ),
    )
    public suspend fun whoAmI(): String =
        execute(
            command = KedisServerCommands.whoAmI(),
        )

    /**
     * @see [KedisServerCommands.info]
     */
    @Deprecated(
        message = "Use commands",
        replaceWith = ReplaceWith(
            expression = "execute(\n" +
                "    command = KedisServerCommands.info(\n" +
                "        section = section,\n" +
                "    ),\n" +
                ")",
            "io.github.domgew.kedis.commands.KedisServerCommands",
        ),
    )
    public suspend fun info(
        vararg section: InfoSectionName,
    ): List<InfoSection> =
        execute(
            command = KedisServerCommands.info(
                section = section,
            ),
        )

    /**
     * @see [KedisServerCommands.infoMap]
     */
    @Deprecated(
        message = "Use commands",
        replaceWith = ReplaceWith(
            expression = "execute(\n" +
                "    command = KedisServerCommands.infoMap(\n" +
                "        section = section,\n" +
                "    ),\n" +
                ")",
            "io.github.domgew.kedis.commands.KedisServerCommands",
        ),
    )
    public suspend fun infoMap(
        vararg section: InfoSectionName,
    ): Map<String?, Map<String, String>> =
        execute(
            command = KedisServerCommands.infoMap(
                section = section,
            ),
        )

    /**
     * @see [KedisServerCommands.infoRaw]
     */
    @Deprecated(
        message = "Use commands",
        replaceWith = ReplaceWith(
            expression = "execute(\n" +
                "    command = KedisServerCommands.infoRaw(\n" +
                "        section = section,\n" +
                "    ),\n" +
                ")",
            "io.github.domgew.kedis.commands.KedisServerCommands",
        ),
    )
    public suspend fun infoRaw(
        vararg section: InfoSectionName,
    ): String? =
        execute(
            command = KedisServerCommands.infoRaw(
                section = section,
            ),
        )

    /**
     * @see [KedisServerCommands.flushAll]
     */
    @Deprecated(
        message = "Use commands",
        replaceWith = ReplaceWith(
            expression = "execute(\n" +
                "    command = KedisServerCommands.flushAll(\n" +
                "        sync = sync,\n" +
                "    ),\n" +
                ")",
            "io.github.domgew.kedis.commands.KedisServerCommands",
        ),
    )
    public suspend fun flushAll(
        sync: SyncOption = SyncOption.SYNC,
    ): Boolean =
        execute(
            command = KedisServerCommands.flushAll(
                sync = sync,
            ),
        )

    /**
     * @see [KedisServerCommands.flushDb]
     */
    @Deprecated(
        message = "Use commands",
        replaceWith = ReplaceWith(
            expression = "execute(\n" +
                "    command = KedisServerCommands.flushDb(\n" +
                "        sync = sync,\n" +
                "    ),\n" +
                ")",
            "io.github.domgew.kedis.commands.KedisServerCommands",
        ),
    )
    public suspend fun flushDb(
        sync: SyncOption = SyncOption.SYNC,
    ): Boolean =
        execute(
            command = KedisServerCommands.flushDb(
                sync = sync,
            ),
        )

    /**
     * @see [KedisServerCommands.bgSave]
     */
    @Deprecated(
        message = "Use commands",
        replaceWith = ReplaceWith(
            expression = "execute(\n" +
                "    command = KedisServerCommands.bgSave(\n" +
                "        schedule = schedule,\n" +
                "    ),\n" +
                ")",
            "io.github.domgew.kedis.commands.KedisServerCommands",
        ),
    )
    public suspend fun bgSave(
        schedule: Boolean = false,
    ): BgSaveResult =
        execute(
            command = KedisServerCommands.bgSave(
                schedule = schedule,
            ),
        )

    /**
     * @see [KedisValueCommands.get]
     */
    @Deprecated(
        message = "Use commands",
        replaceWith = ReplaceWith(
            expression = "execute(\n" +
                "    command = KedisValueCommands.get(\n" +
                "        key = key,\n" +
                "    ),\n" +
                ")",
            "io.github.domgew.kedis.commands.KedisValueCommands",
        ),
    )
    public suspend fun get(
        key: String,
    ): String? =
        execute(
            command = KedisValueCommands.get(
                key = key,
            ),
        )

    /**
     * @see [KedisValueCommands.getBinary]
     */
    @Deprecated(
        message = "Use commands",
        replaceWith = ReplaceWith(
            expression = "execute(\n" +
                "    command = KedisValueCommands.getBinary(\n" +
                "        key = key,\n" +
                "    ),\n" +
                ")",
            "io.github.domgew.kedis.commands.KedisValueCommands",
        ),
    )
    public suspend fun getBinary(
        key: String,
    ): ByteArray? =
        execute(
            command = KedisValueCommands.getBinary(
                key = key,
            ),
        )

    /**
     * @see [KedisValueCommands.getRange]
     */
    @Deprecated(
        message = "Use commands",
        replaceWith = ReplaceWith(
            expression = "execute(\n" +
                "    command = KedisValueCommands.getRange(\n" +
                "        key = key,\n" +
                "        start = start,\n" +
                "        end = end,\n" +
                "    ),\n" +
                ")",
            "io.github.domgew.kedis.commands.KedisValueCommands",
        ),
    )
    public suspend fun getRange(
        key: String,
        start: Long,
        end: Long,
    ): String =
        execute(
            command = KedisValueCommands.getRange(
                key = key,
                start = start,
                end = end,
            ),
        )

    /**
     * @see [KedisValueCommands.getRange]
     */
    @Deprecated(
        message = "Use commands",
        replaceWith = ReplaceWith(
            expression = "execute(\n" +
                "    command = KedisValueCommands.getRange(\n" +
                "        key = key,\n" +
                "        range = range,\n" +
                "    ),\n" +
                ")",
            "io.github.domgew.kedis.commands.KedisValueCommands",
        ),
    )
    public suspend fun getRange(
        key: String,
        range: LongRange,
    ): String =
        execute(
            command = KedisValueCommands.getRange(
                key = key,
                range = range,
            ),
        )

    /**
     * @see [KedisValueCommands.set]
     */
    @Deprecated(
        message = "Use commands",
        replaceWith = ReplaceWith(
            expression = "execute(\n" +
                "    command = KedisValueCommands.set(\n" +
                "        key = key,\n" +
                "        value = value,\n" +
                "        options = options,\n" +
                "    ),\n" +
                ")",
            "io.github.domgew.kedis.commands.KedisValueCommands",
        ),
    )
    public suspend fun set(
        key: String,
        value: String,
        options: SetOptions = SetOptions(),
    ): SetResult =
        execute(
            command = KedisValueCommands.set(
                key = key,
                value = value,
                options = options,
            ),
        )

    /**
     * @see [KedisValueCommands.setBinary]
     */
    @Deprecated(
        message = "Use commands",
        replaceWith = ReplaceWith(
            expression = "execute(\n" +
                "    command = KedisValueCommands.setBinary(\n" +
                "        key = key,\n" +
                "        value = value,\n" +
                "        options = options,\n" +
                "    ),\n" +
                ")",
            "io.github.domgew.kedis.commands.KedisValueCommands",
        ),
    )
    public suspend fun setBinary(
        key: String,
        value: ByteArray,
        options: SetOptions = SetOptions(),
    ): SetBinaryResult =
        execute(
            command = KedisValueCommands.setBinary(
                key = key,
                value = value,
                options = options,
            ),
        )

    /**
     * @see [KedisValueCommands.del]
     */
    @Deprecated(
        message = "Use commands",
        replaceWith = ReplaceWith(
            expression = "execute(\n" +
                "    command = KedisValueCommands.del(\n" +
                "        key = key,\n" +
                "    ),\n" +
                ")",
            "io.github.domgew.kedis.commands.KedisValueCommands",
        ),
    )
    public suspend fun del(
        vararg key: String,
    ): Long =
        execute(
            command = KedisValueCommands.del(
                key = key,
            ),
        )

    /**
     * @see [KedisValueCommands.exists]
     */
    @Deprecated(
        message = "Use commands",
        replaceWith = ReplaceWith(
            expression = "execute(\n" +
                "    command = KedisValueCommands.exists(\n" +
                "        key = key,\n" +
                "    ),\n" +
                ")",
            "io.github.domgew.kedis.commands.KedisValueCommands",
        ),
    )
    public suspend fun exists(
        vararg key: String,
    ): Long =
        execute(
            command = KedisValueCommands.exists(
                key = key,
            ),
        )

    /**
     * @see [KedisValueCommands.expireTime]
     */
    @Deprecated(
        message = "Use commands",
        replaceWith = ReplaceWith(
            expression = "execute(\n" +
                "    command = KedisValueCommands.expireTime(\n" +
                "        key = key,\n" +
                "        inMilliseconds = inMilliseconds,\n" +
                "    ),\n" +
                ")",
            "io.github.domgew.kedis.commands.KedisValueCommands",
        ),
    )
    public suspend fun expireTime(
        key: String,
        inMilliseconds: Boolean = true,
    ): ExpireTimeResult =
        execute(
            command = KedisValueCommands.expireTime(
                key = key,
                inMilliseconds = inMilliseconds,
            ),
        )

    /**
     * @see [KedisValueCommands.ttl]
     */
    @Deprecated(
        message = "Use commands",
        replaceWith = ReplaceWith(
            expression = "execute(\n" +
                "    command = KedisValueCommands.ttl(\n" +
                "        key = key,\n" +
                "        inMilliseconds = inMilliseconds,\n" +
                "    ),\n" +
                ")",
            "io.github.domgew.kedis.commands.KedisValueCommands",
        ),
    )
    public suspend fun ttl(
        key: String,
        inMilliseconds: Boolean = true,
    ): TtlResult =
        execute(
            command = KedisValueCommands.ttl(
                key = key,
                inMilliseconds = inMilliseconds,
            ),
        )

    /**
     * @see [KedisValueCommands.append]
     */
    @Deprecated(
        message = "Use commands",
        replaceWith = ReplaceWith(
            expression = "execute(\n" +
                "    command = KedisValueCommands.append(\n" +
                "        key = key,\n" +
                "        value = value,\n" +
                "    ),\n" +
                ")",
            "io.github.domgew.kedis.commands.KedisValueCommands",
        ),
    )
    public suspend fun append(
        key: String,
        value: String,
    ): Long =
        execute(
            command = KedisValueCommands.append(
                key = key,
                value = value,
            ),
        )

    /**
     * @see [KedisValueCommands.strLen]
     */
    @Deprecated(
        message = "Use commands",
        replaceWith = ReplaceWith(
            expression = "execute(\n" +
                "    command = KedisValueCommands.strLen(\n" +
                "        key = key,\n" +
                "    ),\n" +
                ")",
            "io.github.domgew.kedis.commands.KedisValueCommands",
        ),
    )
    public suspend fun strLen(
        key: String,
    ): Long =
        execute(
            command = KedisValueCommands.strLen(
                key = key,
            ),
        )

    /**
     * @see [KedisValueCommands.decr]
     */
    @Deprecated(
        message = "Use commands",
        replaceWith = ReplaceWith(
            expression = "execute(\n" +
                "    command = KedisValueCommands.decr(\n" +
                "        key = key,\n" +
                "    ),\n" +
                ")",
            "io.github.domgew.kedis.commands.KedisValueCommands",
        ),
    )
    public suspend fun decr(
        key: String,
    ): Long =
        execute(
            command = KedisValueCommands.decr(
                key = key,
            ),
        )

    /**
     * @see [KedisValueCommands.decrBy]
     */
    @Deprecated(
        message = "Use commands",
        replaceWith = ReplaceWith(
            expression = "execute(\n" +
                "    command = KedisValueCommands.decrBy(\n" +
                "        key = key,\n" +
                "        by = by,\n" +
                "    ),\n" +
                ")",
            "io.github.domgew.kedis.commands.KedisValueCommands",
        ),
    )
    public suspend fun decrBy(
        key: String,
        by: Long,
    ): Long =
        execute(
            command = KedisValueCommands.decrBy(
                key = key,
                by = by,
            ),
        )

    /**
     * @see [KedisValueCommands.incr]
     */
    @Deprecated(
        message = "Use commands",
        replaceWith = ReplaceWith(
            expression = "execute(\n" +
                "    command = KedisValueCommands.incr(\n" +
                "        key = key,\n" +
                "    ),\n" +
                ")",
            "io.github.domgew.kedis.commands.KedisValueCommands",
        ),
    )
    public suspend fun incr(
        key: String,
    ): Long =
        execute(
            command = KedisValueCommands.incr(
                key = key,
            ),
        )

    /**
     * @see [KedisValueCommands.incrBy]
     */
    @Deprecated(
        message = "Use commands",
        replaceWith = ReplaceWith(
            expression = "execute(\n" +
                "    command = KedisValueCommands.incrBy(\n" +
                "        key = key,\n" +
                "        by = by,\n" +
                "    ),\n" +
                ")",
            "io.github.domgew.kedis.commands.KedisValueCommands",
        ),
    )
    public suspend fun incrBy(
        key: String,
        by: Long,
    ): Long =
        execute(
            command = KedisValueCommands.incrBy(
                key = key,
                by = by,
            ),
        )

    /**
     * @see [KedisValueCommands.incrByFloat]
     */
    @Deprecated(
        message = "Use commands",
        replaceWith = ReplaceWith(
            expression = "execute(\n" +
                "    command = KedisValueCommands.incrByFloat(\n" +
                "        key = key,\n" +
                "        by = by,\n" +
                "    ),\n" +
                ")",
            "io.github.domgew.kedis.commands.KedisValueCommands",
        ),
    )
    public suspend fun incrByFloat(
        key: String,
        by: Double = 1.0,
    ): Double =
        execute(
            command = KedisValueCommands.incrByFloat(
                key = key,
                by = by,
            ),
        )

    /**
     * @see [KedisHashCommands.hashGet]
     */
    @Deprecated(
        message = "Use commands",
        replaceWith = ReplaceWith(
            expression = "execute(\n" +
                "    command = KedisHashCommands.hashGet(\n" +
                "        key = key,\n" +
                "        field = field,\n" +
                "    ),\n" +
                ")",
            "io.github.domgew.kedis.commands.KedisHashCommands",
        ),
    )
    public suspend fun hashGet(
        key: String,
        field: String,
    ): String? =
        execute(
            command = KedisHashCommands.hashGet(
                key = key,
                field = field,
            ),
        )

    /**
     * @see [KedisHashCommands.hashGetBinary]
     */
    @Deprecated(
        message = "Use commands",
        replaceWith = ReplaceWith(
            expression = "execute(\n" +
                "    command = KedisHashCommands.hashGetBinary(\n" +
                "        key = key,\n" +
                "        field = field,\n" +
                "    ),\n" +
                ")",
            "io.github.domgew.kedis.commands.KedisHashCommands",
        ),
    )
    public suspend fun hashGetBinary(
        key: String,
        field: String,
    ): ByteArray? =
        execute(
            command = KedisHashCommands.hashGetBinary(
                key = key,
                field = field,
            ),
        )

    /**
     * @see [KedisHashCommands.hashGetAll]
     */
    @Deprecated(
        message = "Use commands",
        replaceWith = ReplaceWith(
            expression = "execute(\n" +
                "    command = KedisHashCommands.hashGetAll(\n" +
                "        key = key,\n" +
                "    ),\n" +
                ")",
            "io.github.domgew.kedis.commands.KedisHashCommands",
        ),
    )
    public suspend fun hashGetAll(
        key: String,
    ): Map<String, String>? =
        execute(
            command = KedisHashCommands.hashGetAll(
                key = key,
            ),
        )

    /**
     * @see [KedisHashCommands.hashGetAllBinary]
     */
    @Deprecated(
        message = "Use commands",
        replaceWith = ReplaceWith(
            expression = "execute(\n" +
                "    command = KedisHashCommands.hashGetAllBinary(\n" +
                "        key = key,\n" +
                "    ),\n" +
                ")",
            "io.github.domgew.kedis.commands.KedisHashCommands",
        ),
    )
    public suspend fun hashGetAllBinary(
        key: String,
    ): Map<String, ByteArray>? =
        execute(
            command = KedisHashCommands.hashGetAllBinary(
                key = key,
            ),
        )

    /**
     * @see [KedisHashCommands.hashSet]
     */
    @Deprecated(
        message = "Use commands",
        replaceWith = ReplaceWith(
            expression = "execute(\n" +
                "    command = KedisHashCommands.hashSet(\n" +
                "        key = key,\n" +
                "        fieldValues = fieldValues,\n" +
                "    ),\n" +
                ")",
            "io.github.domgew.kedis.commands.KedisHashCommands",
        ),
    )
    public suspend fun hashSet(
        key: String,
        fieldValues: Map<String, String>,
    ): Long =
        execute(
            command = KedisHashCommands.hashSet(
                key = key,
                fieldValues = fieldValues,
            ),
        )

    /**
     * @see [KedisHashCommands.hashSetBinary]
     */
    @Deprecated(
        message = "Use commands",
        replaceWith = ReplaceWith(
            expression = "execute(\n" +
                "    command = KedisHashCommands.hashSetBinary(\n" +
                "        key = key,\n" +
                "        fieldValues = fieldValues,\n" +
                "    ),\n" +
                ")",
            "io.github.domgew.kedis.commands.KedisHashCommands",
        ),
    )
    public suspend fun hashSetBinary(
        key: String,
        fieldValues: Map<String, ByteArray>,
    ): Long =
        execute(
            command = KedisHashCommands.hashSetBinary(
                key = key,
                fieldValues = fieldValues,
            ),
        )

    /**
     * @see [KedisHashCommands.hashDel]
     */
    @Deprecated(
        message = "Use commands",
        replaceWith = ReplaceWith(
            expression = "execute(\n" +
                "    command = KedisHashCommands.hashDel(\n" +
                "        key = key,\n" +
                "        field = field,\n" +
                "    ),\n" +
                ")",
            "io.github.domgew.kedis.commands.KedisHashCommands",
        ),
    )
    public suspend fun hashDel(
        key: String,
        vararg field: String,
    ): Long =
        execute(
            command = KedisHashCommands.hashDel(
                key = key,
                field = field,
            ),
        )

    /**
     * @see [KedisHashCommands.hashExists]
     */
    @Deprecated(
        message = "Use commands",
        replaceWith = ReplaceWith(
            expression = "execute(\n" +
                "    command = KedisHashCommands.hashExists(\n" +
                "        key = key,\n" +
                "        field = field,\n" +
                "    ),\n" +
                ")",
            "io.github.domgew.kedis.commands.KedisHashCommands",
        ),
    )
    public suspend fun hashExists(
        key: String,
        field: String,
    ): Boolean =
        execute(
            command = KedisHashCommands.hashExists(
                key = key,
                field = field,
            ),
        )

    /**
     * @see [KedisHashCommands.hashKeys]
     */
    @Deprecated(
        message = "Use commands",
        replaceWith = ReplaceWith(
            expression = "execute(\n" +
                "    command = KedisHashCommands.hashKeys(\n" +
                "        key = key,\n" +
                "    ),\n" +
                ")",
            "io.github.domgew.kedis.commands.KedisHashCommands",
        ),
    )
    public suspend fun hashKeys(
        key: String,
    ): List<String>? =
        execute(
            command = KedisHashCommands.hashKeys(
                key = key,
            ),
        )

    /**
     * @see [KedisHashCommands.hashLength]
     */
    @Deprecated(
        message = "Use commands",
        replaceWith = ReplaceWith(
            expression = "execute(\n" +
                "    command = KedisHashCommands.hashLength(\n" +
                "        key = key,\n" +
                "    ),\n" +
                ")",
            "io.github.domgew.kedis.commands.KedisHashCommands",
        ),
    )
    public suspend fun hashLength(
        key: String,
    ): Long =
        execute(
            command = KedisHashCommands.hashLength(
                key = key,
            ),
        )
}
