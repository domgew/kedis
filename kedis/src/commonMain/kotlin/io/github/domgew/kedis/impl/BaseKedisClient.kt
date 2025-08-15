package io.github.domgew.kedis.impl

import io.github.domgew.kedis.KedisConfiguration
import io.github.domgew.kedis.KedisException
import io.github.domgew.kedis.commands.KedisFullCommand
import io.github.domgew.kedis.commands.server.AuthCommand
import io.github.domgew.kedis.commands.server.SelectCommand
import io.ktor.network.selector.SelectorManager
import io.ktor.network.sockets.InetSocketAddress
import io.ktor.network.sockets.SocketAddress
import io.ktor.network.sockets.UnixSocketAddress
import io.ktor.utils.io.CancellationException
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

internal class BaseKedisClient(
    private val selectorManager: SelectorManager,
    private val configuration: KedisConfiguration,
) : AutoCloseable {

    private val _connection = lazy {
        KedisConnection(
            selectorManager = selectorManager,
            socketAddress = configuration.socketAddress,
            connectionTimeout = configuration.connectionTimeout,
            keepAlive = configuration.keepAlive,
            connected = ::connected,
        )
    }
    private val _readLock = Mutex()
    private val _writeLock = Mutex()

    var selectedDatabase: Int = configuration.databaseIndex
        private set

    val probablyConnected: Boolean
        get() =
            _connection.isInitialized()
                && _connection.value
                .probablyConnected

    suspend fun connect() {
        _connection.value.connect()
    }

    @OptIn(ExperimentalContracts::class)
    suspend fun <T> useExclusively(
        block: suspend (
            usableClient: UsableClient,
        ) -> T,
    ): T {
        contract {
            callsInPlace(block, InvocationKind.EXACTLY_ONCE)
        }

        return _connection.value.useExclusively { connection ->
            try {
                block(
                    UsableClientImpl(
                        connection = connection,
                    ),
                )
            } catch (ex: CancellationException) {
                // no partial reads/writes
                _connection.value.close()
                throw ex
            } catch (ex: KedisException.GenericNetworkException) {
                _connection.value.close()
                throw ex
            }
        }
    }

    private suspend fun writeCommand(
        connection: KedisConnection.UsableConnection,
        command: KedisFullCommand<*>,
        flush: Boolean,
    ) {
        _writeLock.withLock {
            doWriteCommand(
                connection = connection,
                command = command,
            )
            if (flush) {
                doFlush(
                    connection = connection,
                )
            }
        }
    }

    private suspend fun <T> readCommand(
        connection: KedisConnection.UsableConnection,
        command: KedisFullCommand<T>
    ) =
        _readLock.withLock {
            doReadCommand(
                connection = connection,
                command = command,
            )
        }

    private suspend fun flush(
        connection: KedisConnection.UsableConnection,
    ) =
        _writeLock.withLock {
            doFlush(
                connection = connection,
            )
        }

    private suspend fun doWriteCommand(
        connection: KedisConnection.UsableConnection,
        command: KedisFullCommand<*>,
    ) {
        commoniseNetworkExceptions {
            command.toRedisRequest()
                .writeTo(connection.writeChannel)
        }
    }

    private suspend fun doFlush(
        connection: KedisConnection.UsableConnection,
    ) {
        commoniseNetworkExceptions {
            connection.writeChannel
                .flush()
        }
    }

    private suspend fun <T> doReadCommand(
        connection: KedisConnection.UsableConnection,
        command: KedisFullCommand<T>,
    ): T {
        commoniseNetworkExceptions {
            val response = RedisMessage.readNonAttributes(connection.readChannel)

            return command.fromRedisResponse(
                response = response,
            )
                .also {
                    if (command is SelectCommand) {
                        selectedDatabase = command.databaseIndex
                    }
                }
        }
    }

    private suspend fun connected(
        connection: KedisConnection.UsableConnection,
    ): Throwable? {
        selectedDatabase = 0
        autoAuth(
            connection = connection,
        )
            ?.also {
                return it
            }
        autoSelectDatabase(
            connection = connection,
        )
            ?.also {
                return it
            }

        return null
    }

    private suspend fun autoAuth(
        connection: KedisConnection.UsableConnection,
    ): Throwable? =
        when (
            val auth = configuration.authentication
        ) {
            KedisConfiguration.Authentication.NoAutoAuth ->
                null

            is KedisConfiguration.Authentication.AutoAuth -> {
                runCatching {
                    val command = AuthCommand(
                        username = auth.username,
                        password = auth.password,
                    )

                    doWriteCommand(
                        connection = connection,
                        command = command,
                    )
                    doFlush(
                        connection = connection,
                    )
                    doReadCommand(
                        connection = connection,
                        command = command,
                    )
                }
                    .exceptionOrNull()
            }
        }

    private suspend fun autoSelectDatabase(
        connection: KedisConnection.UsableConnection,
    ): Throwable? =
        when (
            val index = configuration.databaseIndex
        ) {
            0 ->
                null

            else ->
                runCatching {
                    val command = SelectCommand(
                        databaseIndex = index,
                    )

                    doWriteCommand(
                        connection = connection,
                        command = command,
                    )
                    doFlush(
                        connection = connection,
                    )
                    doReadCommand(
                        connection = connection,
                        command = command,
                    )
                }
                    .exceptionOrNull()
        }

    override fun close() {
        if (!_connection.isInitialized()) {
            return
        }

        _connection.value
            .close()
    }

    private val KedisConfiguration.socketAddress: SocketAddress
        get() =
            when (endpoint) {
                is KedisConfiguration.Endpoint.HostPort ->
                    InetSocketAddress(
                        hostname = endpoint.host,
                        port = endpoint.port,
                    )

                is KedisConfiguration.Endpoint.UnixSocket ->
                    UnixSocketAddress(
                        path = endpoint.path,
                    )
            }

    interface UsableClient {

        suspend fun write(
            command: KedisFullCommand<*>,
            flush: Boolean = true,
        )

        suspend fun <T> read(
            command: KedisFullCommand<T>,
        ): T

        suspend fun flush()
    }

    private inner class UsableClientImpl(
        private val connection: KedisConnection.UsableConnection,
    ) : UsableClient {

        override suspend fun write(
            command: KedisFullCommand<*>,
            flush: Boolean,
        ) {
            writeCommand(
                connection = connection,
                command = command,
                flush = flush,
            )
        }

        override suspend fun <T> read(
            command: KedisFullCommand<T>,
        ): T =
            readCommand(
                connection = connection,
                command = command,
            )

        override suspend fun flush() {
            flush(
                connection = connection,
            )
        }
    }
}
