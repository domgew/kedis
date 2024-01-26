package io.github.domgew.kedis

import java.net.ConnectException

internal actual suspend fun <T> commoniseConnectException(
    block: suspend () -> T,
): T =
    try {
        commoniseNetworkExceptions(
            block = block,
        )
    } catch (ex: ConnectException) {
        throw KedisException.ConnectException(
            cause = ex,
        )
    }

internal actual suspend fun <T> commoniseNetworkExceptions(
    block: suspend () -> T,
): T =
    try {
        block()
    } catch (ex: kotlinx.coroutines.channels.ClosedReceiveChannelException) {
        throw KedisException.GenericNetworkException(
            cause = ex,
        )
    } catch (ex: java.io.IOException) {
        throw KedisException.GenericNetworkException(
            cause = ex,
        )
    }
