package io.github.domgew.kedis

internal actual suspend fun <T> commoniseConnectException(
    block: suspend () -> T,
): T =
    try {
        block()
    } catch (ex: IllegalStateException) {
        if (
            ex.message?.contains("Failed", ignoreCase = true) != true
            || ex.message?.contains("connect", ignoreCase = true) != true
        ) {
            throw ex
        }

        throw KedisException.ConnectException(
            cause = ex,
        )
    }
