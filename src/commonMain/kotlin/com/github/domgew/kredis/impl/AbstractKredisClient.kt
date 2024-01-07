package com.github.domgew.kredis.impl

import com.github.domgew.kredis.KredisClient
import com.github.domgew.kredis.KredisConfiguration
import com.github.domgew.kredis.KredisException
import com.github.domgew.kredis.commands.KredisCommand
import io.ktor.network.selector.SelectorManager
import io.ktor.network.sockets.InetSocketAddress
import io.ktor.network.sockets.Socket
import io.ktor.network.sockets.aSocket
import io.ktor.network.sockets.awaitClosed
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

internal abstract class AbstractKredisClient(
    protected val configuration: KredisConfiguration,
): KredisClient {
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
            throw KredisException.ConnectionTimeout
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
        _socket?.close()
        _socket?.awaitClosed()
    }

    protected suspend fun executeCommand(
        command: KredisCommand,
    ): RedisMessage {
        ensureConnected()
        command.toRedisMessage().writeTo(_writeChannel!!)
        _writeChannel!!.flush()
        return RedisMessage.parse(_readChannel!!)
    }
}
