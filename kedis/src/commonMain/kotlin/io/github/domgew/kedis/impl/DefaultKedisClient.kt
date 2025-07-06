package io.github.domgew.kedis.impl

import io.github.domgew.kedis.KedisClient
import io.github.domgew.kedis.KedisConfiguration
import io.github.domgew.kedis.KedisPipelineClient
import io.github.domgew.kedis.commands.KedisCommand
import io.github.domgew.kedis.commands.KedisFullCommand
import io.ktor.network.selector.SelectorManager

internal class DefaultKedisClient(
    private val selectorManager: SelectorManager,
    private val configuration: KedisConfiguration,
) : KedisClient,
    AutoCloseable {

    private val _client = lazy {
        BaseKedisClient(
            selectorManager = selectorManager,
            configuration = configuration,
        )
    }

    override val probablyConnected: Boolean
        get() =
            _client.isInitialized()
                && _client.value
                .probablyConnected

    override val isConnected: Boolean by ::probablyConnected

    override val selectedDatabase: Int
        get() =
            when {
                _client.isInitialized() ->
                    _client.value
                        .selectedDatabase

                else ->
                    0
            }

    override suspend fun connect() {
        _client.value
            .connect()
    }

    override suspend fun <T> execute(
        command: KedisCommand<T>,
    ): T =
        doExecute(
            command = command as KedisFullCommand<T>,
        )

    override fun pipelined(): KedisPipelineClient =
        DefaultKedisPipelineClient(
            client = _client,
        )

    override fun close() {
        if (!_client.isInitialized()) {
            return
        }

        _client.value
            .close()
    }

    private suspend fun <T> doExecute(
        command: KedisFullCommand<T>,
    ): T =
        _client.value.useExclusively { client ->
            client.write(
                command = command,
            )

            return@useExclusively client.read(
                command = command,
            )
        }

}
