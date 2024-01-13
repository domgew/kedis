package io.github.domgew.kedis

import io.github.domgew.kedis.arguments.SetOptions
import io.github.domgew.kedis.arguments.SyncOption
import io.github.domgew.kedis.impl.DefaultKedisClient

/**
 * Each connection should have its own instance.
 *
 * Should be used with `client.use { ... }`.
 */
@OptIn(ExperimentalStdlibApi::class)
public interface KedisClient: AutoCloseable {
    public companion object {
        public fun newClient(configuration: KedisConfiguration): KedisClient {
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
     * @return The response - should be the [content]
     */
    public suspend fun ping(
        content: String = "PING",
    ): String?

    /**
     * @return The redis server version
     */
    public suspend fun serverVersion(): String

    /**
     * Clears all redis DBs
     * @return Whether the server responded with "OK"
     */
    public suspend fun flushAll(sync: SyncOption = SyncOption.SYNC): Boolean

    /**
     * Clears the current redis DB
     * @return Whether the server responded with "OK"
     */
    public suspend fun flushDb(sync: SyncOption = SyncOption.SYNC): Boolean

    /**
     * Gets the value behind the given [key].
     * @return The value or NULL
     */
    public suspend fun get(
        key: String,
    ): String?

    /**
     * Sets the value behind the given [key], mind the [options].
     * @return The previous value if requested
     */
    public suspend fun set(
        key: String,
        value: String,
        options: SetOptions = SetOptions(),
    ): String?

    /**
     * Removes the provided [key]s. If a key does not exist, no error is thrown.
     * @return The number of removed provided [key]s
     */
    public suspend fun del(
        vararg key: String,
    ): Long

    /**
     * Checks whether the given [key]s exist.
     * @return The number of provided [key]s that do exist
     */
    public suspend fun exists(
        vararg key: String,
    ): Long
}
