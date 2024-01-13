package com.github.domgew.kedis

import com.github.domgew.kedis.arguments.SyncOptions
import com.github.domgew.kedis.impl.DefaultKedisClient

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
    public suspend fun flushAll(sync: SyncOptions): Boolean

    /**
     * Gets the value behind the given [key].
     * @return The value or NULL
     */
    public suspend fun get(
        key: String,
    ): String?

    // TODO: add options and improve result
    public suspend fun set(
        key: String,
        value: String,
    ): String?
}
