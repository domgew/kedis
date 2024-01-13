package io.github.domgew.kedis.impl

import io.github.domgew.kedis.KedisClient
import io.github.domgew.kedis.KedisConfiguration
import io.github.domgew.kedis.KedisException
import io.github.domgew.kedis.commands.KedisCommand
import io.ktor.network.selector.SelectorManager
import io.ktor.network.sockets.InetSocketAddress
import io.ktor.network.sockets.Socket
import io.ktor.network.sockets.aSocket
import io.ktor.network.sockets.openReadChannel
import io.ktor.network.sockets.openWriteChannel
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.ByteWriteChannel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.isActive
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.withTimeout

internal abstract class AbstractKedisClient(
    protected val configuration: KedisConfiguration,
): KedisClient {
    protected val lock = Mutex()
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
                        remoteAddress = InetSocketAddress(
                            hostname = configuration.host,
                            port = configuration.port,
                        ),
                        configure = {
                            keepAlive = configuration.keepAlive
                        },
                    )
            }
        } catch (ex: TimeoutCancellationException) {
            throw KedisException.ConnectionTimeout
        }
        this._socket = socket
        this._readChannel = socket.openReadChannel()
        this._writeChannel = socket.openWriteChannel(autoFlush = false)
    }

    protected suspend fun ensureConnected() {
        if (!isConnected) {
            doConnect()
        }
    }

    protected suspend fun doClose() {
        _socket?.dispose()
    }

    protected suspend fun executeCommand(
        command: KedisCommand,
    ): RedisMessage {
        ensureConnected()
        command.toRedisMessage().writeTo(_writeChannel!!)
        _writeChannel!!.flush()
        return RedisMessage.parse(_readChannel!!)
    }
}
