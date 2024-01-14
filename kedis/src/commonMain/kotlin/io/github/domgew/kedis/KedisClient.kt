package io.github.domgew.kedis

import io.github.domgew.kedis.arguments.InfoSectionName
import io.github.domgew.kedis.arguments.SetOptions
import io.github.domgew.kedis.arguments.SyncOption
import io.github.domgew.kedis.impl.DefaultKedisClient
import io.github.domgew.kedis.results.server.InfoSection
import io.github.domgew.kedis.results.value.SetResult

@OptIn(ExperimentalStdlibApi::class)
public interface KedisClient: AutoCloseable {
    public companion object {
        /**
         * When you connect the client, make sure to disconnect it again. Each command will connect, when the connection is not already open.
         */
        public fun newClient(
            configuration: KedisConfiguration,
        ): KedisClient {
            return DefaultKedisClient(
                configuration = configuration,
            )
        }
    }

    public val isConnected: Boolean
    public suspend fun connect()
    public suspend fun closeSuspended()

    /**
     * Sends a message to the server which should be returned unchanged.
     *
     * [https://redis.io/commands/ping/](https://redis.io/commands/ping/)
     * @return The response - should be the [content]
     */
    public suspend fun ping(
        content: String = "PING",
    ): String

    /**
     * [https://redis.io/commands/info/](https://redis.io/commands/info/)
     * @return The requested information
     */
    public suspend fun info(
        vararg section: InfoSectionName,
    ): List<InfoSection>

    /**
     * [https://redis.io/commands/info/](https://redis.io/commands/info/)
     * @return The requested information - the first key is the lowercase section name, the second the actual field
     */
    public suspend fun infoMap(
        vararg section: InfoSectionName,
    ): Map<String?, Map<String, String>>

    /**
     * [https://redis.io/commands/info/](https://redis.io/commands/info/)
     * @return The requested information
     */
    public suspend fun infoRaw(
        vararg section: InfoSectionName,
    ): String?

    /**
     * Clears all redis DBs.
     *
     * [https://redis.io/commands/flushall/](https://redis.io/commands/flushall/)
     * @return Whether the server responded with "OK"
     */
    public suspend fun flushAll(
        sync: SyncOption = SyncOption.SYNC,
    ): Boolean

    /**
     * Clears the current redis DB.
     *
     * [https://redis.io/commands/flushdb/](https://redis.io/commands/flushdb/)
     * @return Whether the server responded with "OK"
     */
    public suspend fun flushDb(
        sync: SyncOption = SyncOption.SYNC,
    ): Boolean

    /**
     * Gets the value behind the given [key].
     *
     * [https://redis.io/commands/get/](https://redis.io/commands/get/)
     * @return The value or NULL
     */
    public suspend fun get(
        key: String,
    ): String?

    /**
     * Sets the value behind the given [key], minding the [options].
     *
     * [https://redis.io/commands/set/](https://redis.io/commands/set/)
     * @return Whether the operation was successful and the previous value if requested
     */
    public suspend fun set(
        key: String,
        value: String,
        options: SetOptions = SetOptions(),
    ): SetResult

    /**
     * Removes the provided [key]s. If a key does not exist, no error is thrown.
     *
     * [https://redis.io/commands/del/](https://redis.io/commands/del/)
     * @return The number of removed provided [key]s
     */
    public suspend fun del(
        vararg key: String,
    ): Long

    /**
     * Checks whether the given [key]s exist.
     *
     * [https://redis.io/commands/exists/](https://redis.io/commands/exists/)
     * @return The number of provided [key]s that do exist
     */
    public suspend fun exists(
        vararg key: String,
    ): Long
}