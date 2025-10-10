package io.github.domgew.kedis

import io.github.domgew.kedis.KedisClient.Companion.builder
import io.github.domgew.kedis.KedisClient.Companion.invoke
import io.github.domgew.kedis.KedisClient.Companion.newClient
import io.github.domgew.kedis.commands.KedisCommand
import io.github.domgew.kedis.commands.KedisServerCommands
import io.github.domgew.kedis.impl.DefaultKedisClient
import io.ktor.network.selector.SelectorManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO

/**
 * The public interface of the client.
 * Use [KedisClient.newClient], [KedisClient.builder], or [KedisClient.invoke] to create an instance.
 * See [io.github.domgew.kedis.commands] for available commands.
 *
 * @see KedisClient.newClient
 * @see KedisClient.invoke
 * @see KedisClient.builder
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
         * @see builder
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
         * @see builder
         */
        public operator fun invoke(
            configuration: KedisConfiguration,
        ): KedisClient =
            newClient(
                configuration = configuration,
            )

        /**
         * Creates a new client instance without connecting.
         *
         * When you connect the client, make sure to disconnect/[close] it again.
         * Each command (method) will connect automatically, when the connection is not already open.
         *
         * @see newClient
         * @see invoke
         * @throws KedisBuilder.MissingConfig
         */
        public fun builder(
            config: KedisBuilder.() -> Unit,
        ): KedisClient =
            KedisBuilder()
                .also {
                    it.config()
                }
                .build()

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
}
