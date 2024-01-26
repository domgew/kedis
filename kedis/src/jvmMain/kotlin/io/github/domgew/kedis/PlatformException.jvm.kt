package io.github.domgew.kedis

import java.net.ConnectException

internal actual suspend fun <T> commoniseConnectException(
    block: suspend () -> T,
): T =
    try {
        block()
    } catch (ex: ConnectException) {
        throw KedisException.ConnectException(
            cause = ex,
        )
    }
