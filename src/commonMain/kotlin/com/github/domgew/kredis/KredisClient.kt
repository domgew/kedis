package com.github.domgew.kredis

import com.github.domgew.kredis.arguments.SyncOptions
import com.github.domgew.kredis.impl.DefaultKredisClient

/**
 * Each connection should have its own instance.
 *
 * Should be used with `client.use { ... }`.
 */
@OptIn(ExperimentalStdlibApi::class)
public interface KredisClient: AutoCloseable {
    public companion object {
        public fun newClient(configuration: KredisConfiguration): KredisClient {
            return DefaultKredisClient(
                configuration = configuration,
            )
        }
    }

    public val isConnected: Boolean
    public suspend fun connect()
    public suspend fun closeAsync()

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
