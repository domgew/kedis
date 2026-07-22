package io.github.domgew.kedis.impl

import io.github.domgew.kedis.KedisException
import io.ktor.network.selector.SelectorManager
import io.ktor.network.sockets.Connection
import io.ktor.network.sockets.SocketAddress
import io.ktor.network.sockets.aSocket
import io.ktor.network.sockets.connection
import io.ktor.network.sockets.isClosed
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.ByteWriteChannel
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.time.Duration
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.internal.SynchronizedObject
import kotlinx.coroutines.internal.synchronized
import kotlinx.coroutines.isActive
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withTimeoutOrNull

@OptIn(InternalCoroutinesApi::class)
internal class KedisConnection(
    private val selectorManager: SelectorManager,
    private val socketAddress: SocketAddress,
    private val connectionTimeout: Duration = Duration.INFINITE,
    private val keepAlive: Boolean = true,
    private val connected: suspend (UsableConnection) -> Throwable? = {
        null
    },
) : AutoCloseable {

    private var _sync = SynchronizedObject()
    private var _connection: Connection? = null
    private val _connectionUse = Mutex()

    val probablyConnected: Boolean
        get() =
            synchronized(_sync) {
                _connection != null
                    && _connection?.socket?.isActive == true
                    && _connection?.socket?.isClosed == false
                    && _connection?.input?.isClosedForRead == false
                    && _connection?.output?.isClosedForWrite == false
            }

    suspend fun connect() {
        if (probablyConnected) {
            return
        }

        useExclusively {
            // don't do anything
        }
    }

    @OptIn(ExperimentalContracts::class)
    suspend fun <T> useExclusively(
        block: suspend (
            connection: UsableConnection,
        ) -> T,
    ): T {
        contract {
            callsInPlace(block, InvocationKind.EXACTLY_ONCE)
        }

        _connectionUse.withLock {
            var conn: Connection? = _connection
            if (!probablyConnected) {
                conn = doConnect()
            }
            if (conn == null) {
                throw KedisException.ConnectException(
                    Exception("No connection"),
                )
            }

            return block(
                usableConnection(
                    connection = conn,
                ),
            )
        }
    }

    override fun close() {
        synchronized(_sync) {
            if (_connection != null) {
                disposeConnection()
            }
        }
    }

    private suspend fun doConnect(): Connection {
        val socket = commoniseConnectException {
            withTimeoutOrNull(connectionTimeout) {
                aSocket(selectorManager)
                    .tcp()
                    .connect(
                        remoteAddress = socketAddress,
                    ) {
                        keepAlive = this@KedisConnection.keepAlive
                    }
            }
                ?: throw KedisException.ConnectionTimeoutException()
        }

        val connection: Connection

        synchronized(_sync) {
            if (_connection != null) {
                disposeConnection()
            }
            connection = socket.connection()
            _connection = connection
        }

        val th = connected(
            usableConnection(
                connection = connection,
            ),
        )

        if (th != null) {
            synchronized(_sync) {
                try {
                    socket.dispose()
                } catch (_: Throwable) {
                    // ignore
                }
                if (_connection === connection) {
                    _connection = null
                }
            }
            throw th
        }

        return connection
    }

    private fun disposeConnection() {
        try {
            _connection?.socket?.dispose()
        } catch (_: Throwable) {
            // ignore
        }
        _connection = null
    }

    private fun usableConnection(
        connection: Connection,
    ): UsableConnection =
        UsableConnectionImpl(
            connection = connection,
        )

    interface UsableConnection {

        val writeChannel: ByteWriteChannel
        val readChannel: ByteReadChannel
    }

    private class UsableConnectionImpl(
        private val connection: Connection,
    ) : UsableConnection {

        override val readChannel by connection::input
        override val writeChannel by connection::output
    }
}
