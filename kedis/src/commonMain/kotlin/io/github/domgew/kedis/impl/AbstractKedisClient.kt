package io.github.domgew.kedis.impl

import io.github.domgew.kedis.KedisClient
import io.github.domgew.kedis.KedisConfiguration
import io.github.domgew.kedis.KedisException
import io.github.domgew.kedis.commands.KedisFullCommand
import io.ktor.network.selector.SelectorManager
import io.ktor.network.sockets.InetSocketAddress
import io.ktor.network.sockets.Socket
import io.ktor.network.sockets.UnixSocketAddress
import io.ktor.network.sockets.aSocket
import io.ktor.network.sockets.openReadChannel
import io.ktor.network.sockets.openWriteChannel
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.ByteWriteChannel
import io.ktor.utils.io.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.isActive
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout

internal abstract class AbstractKedisClient(
    protected val configuration: KedisConfiguration,
): KedisClient {
    private var _socket: Socket? = null
    private var _writeChannel: ByteWriteChannel? = null
    private var _readChannel: ByteReadChannel? = null

    final override val isConnected: Boolean
        get() = _socket != null
                && _writeChannel != null
                && _readChannel != null
                && _socket?.isActive == true

    private suspend fun doConnect() {
        val socket = try {
            withTimeout(configuration.connectionTimeoutMillis) {
                aSocket(SelectorManager(Dispatchers.IO))
                    .tcp()
                    .connect(
                        remoteAddress = when (
                            val endpoint = configuration.endpoint
                        ) {
                            is KedisConfiguration.Endpoint.HostPort ->
                                InetSocketAddress(
                                    hostname = endpoint.host,
                                    port = endpoint.port,
                                )

                            is KedisConfiguration.Endpoint.UnixSocket ->
                                UnixSocketAddress(
                                    path = endpoint.path,
                                )
                        },
                        configure = {
                            keepAlive = configuration.keepAlive
                        },
                    )
            }
        } catch (ex: TimeoutCancellationException) {
            _socket?.dispose()
            throw KedisException.ConnectionTimeout
        }

        _socket = socket
        _readChannel = socket.openReadChannel()
        _writeChannel = socket.openWriteChannel(autoFlush = false)
    }

    protected suspend fun ensureConnected() {
        if (!isConnected) {
            doConnect()
        }
    }

    protected suspend fun doClose() {
        _socket?.dispose()
    }

    protected suspend fun <T> executeCommand(
        command: KedisFullCommand<T>,
    ): T {
        try {
            ensureConnected()

            command.toRedisRequest()
                .writeTo(_writeChannel!!)
            _writeChannel!!.flush()

            return command.fromRedisResponse(
                response = RedisMessage.parse(_readChannel!!),
            )
        } catch (ex: CancellationException) {
            // for preventing partial reads and writes
            runBlocking {
                doClose()
            }
            throw ex
        }
    }
}
