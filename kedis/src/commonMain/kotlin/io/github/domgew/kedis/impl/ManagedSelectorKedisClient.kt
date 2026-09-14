package io.github.domgew.kedis.impl

import io.github.domgew.kedis.KedisClient
import io.github.domgew.kedis.KedisConfiguration
import io.github.domgew.kedis.KedisPipelineClient
import io.github.domgew.kedis.commands.KedisCommand
import io.ktor.network.selector.SelectorManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO

internal class ManagedSelectorKedisClient(
    private val configuration: KedisConfiguration,
) : KedisClient,
    AutoCloseable {

    private val _selectorManager = lazy {
        SelectorManager(
            dispatcher = Dispatchers.IO,
        )
    }
    private val _base = lazy {
        DefaultKedisClient(
            selectorManager = _selectorManager.value,
            configuration = configuration,
        )
    }

    override val isConnected: Boolean
        get() =
            _base.isInitialized()
                && _base.value
                .isConnected

    override val probablyConnected: Boolean
        get() =
            _base.isInitialized()
                && _base.value
                .probablyConnected

    override val selectedDatabase: Int
        get() =
            when {
                _base.isInitialized() ->
                    _base.value
                        .selectedDatabase

                else ->
                    0
            }

    override suspend fun connect() {
        _base.value
            .connect()
    }

    override suspend fun <T> execute(
        command: KedisCommand<T>,
    ): T =
        _base.value
            .execute(
                command = command,
            )

    override fun pipelined(): KedisPipelineClient =
        _base.value
            .pipelined()

    override fun close() {
        if (_base.isInitialized()) {
            try {
                _base.value
                    .close()
            } catch (_: Exception) {
                // ignore
            }
        }
        if (_selectorManager.isInitialized()) {
            try {
                _selectorManager.value
                    .close()
            } catch (_: Exception) {
                // ignore
            }
        }
    }
}
