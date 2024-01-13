package io.github.domgew.kedis.utils

import io.ktor.network.selector.SelectorManager
import io.ktor.network.sockets.InetSocketAddress
import io.ktor.network.sockets.Socket
import io.ktor.network.sockets.SocketAddress
import io.ktor.network.sockets.aSocket
import io.ktor.network.sockets.openReadChannel
import io.ktor.network.sockets.openWriteChannel
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.ByteWriteChannel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.runBlocking

internal object SocketUtil {
    @OptIn(ExperimentalStdlibApi::class)
    suspend fun <T> withConnectedSocket(
        endpoint: SocketAddress = getDefaultEndpoint(),
        block: suspend SocketContext.() -> T,
    ): T {
        val socket = aSocket(SelectorManager(Dispatchers.IO))
            .tcp()
            .connect(
                remoteAddress = endpoint,
            )
        val socketContext = SocketContext(
            socket = socket,
            readChannel = socket.openReadChannel(),
            writeChannel = socket.openWriteChannel(autoFlush = false),
        )

        return socketContext.use {
            block(it)
        }
    }

    private fun getDefaultEndpoint(): SocketAddress {
        return InetSocketAddress(
            hostname = "127.0.0.1",
            port = TestConfigUtil.getPort(),
        )
    }

    @OptIn(ExperimentalStdlibApi::class)
    class SocketContext(
        val socket: Socket,
        val readChannel: ByteReadChannel,
        val writeChannel: ByteWriteChannel,
    ): AutoCloseable {
        override fun close() {
            runBlocking {
                socket.dispose()
            }
        }
    }
}
