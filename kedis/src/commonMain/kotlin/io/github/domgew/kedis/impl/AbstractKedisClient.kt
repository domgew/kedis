package io.github.domgew.kedis.impl

import io.github.domgew.kedis.KedisClient
import io.github.domgew.kedis.KedisConfiguration
import io.github.domgew.kedis.KedisException
import io.github.domgew.kedis.commands.KedisFullCommand
import io.github.domgew.kedis.commands.server.AuthCommand
import io.github.domgew.kedis.commoniseConnectException
import io.github.domgew.kedis.commoniseNetworkExceptions
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
) : KedisClient {
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
            commoniseConnectException {
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
            }
        } catch (ex: TimeoutCancellationException) {
            _socket?.dispose()
            throw KedisException.ConnectionTimeoutException()
        }

        _socket = socket
        _readChannel = socket.openReadChannel()
        _writeChannel = socket.openWriteChannel(autoFlush = false)

        when (configuration.authentication) {
            is KedisConfiguration.Authentication.AutoAuth -> {
                performAuthentication(
                    username = configuration.authentication.username,
                    password = configuration.authentication.password,
                )
            }

            KedisConfiguration.Authentication.NoAutoAuth -> {
                // no authentication required -> don't do anything
            }
        }
    }

    protected suspend fun ensureConnected() {
        if (!isConnected) {
            doConnect()
        }
    }

    protected suspend fun doClose() {
        _socket?.dispose()
    }

    protected suspend fun performAuthentication(
        username: String?,
        password: String,
    ) {
        executeCommand(
            AuthCommand(
                username = username,
                password = password,
            ),
        )
    }

    protected suspend fun <T> executeCommand(
        command: KedisFullCommand<T>,
    ): T {
        try {
            ensureConnected()

            return commoniseNetworkExceptions {
                command.toRedisRequest()
                    .writeTo(_writeChannel!!)
                _writeChannel!!.flush()

                return@commoniseNetworkExceptions command.fromRedisResponse(
                    response = RedisMessage.parse(_readChannel!!),
                )
            }
        } catch (ex: CancellationException) {
            // for preventing partial reads and writes
            runBlocking {
                doClose()
            }
            throw ex
        } catch (ex: KedisException.GenericNetworkException) {
            runBlocking {
                doClose()
            }
            throw ex
        }
    }
}
