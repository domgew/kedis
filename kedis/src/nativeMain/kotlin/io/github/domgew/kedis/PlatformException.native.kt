package io.github.domgew.kedis

internal actual suspend fun <T> commoniseConnectException(
    block: suspend () -> T,
): T =
    try {
        commoniseNetworkExceptions(
            block = block,
        )
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

internal actual suspend fun <T> commoniseNetworkExceptions(
    block: suspend () -> T,
): T =
    try {
        block()
    } catch (ex: io.ktor.utils.io.errors.EOFException) {
        throw KedisException.GenericNetworkException(
            cause = ex,
        )
    } catch (ex: io.ktor.utils.io.core.EOFException) {
        throw KedisException.GenericNetworkException(
            cause = ex,
        )
    } catch (ex: io.ktor.utils.io.errors.IOException) {
        throw KedisException.GenericNetworkException(
            cause = ex,
        )
    } catch (ex: io.ktor.utils.io.errors.PosixException) {
        throw KedisException.GenericNetworkException(
            cause = ex,
        )
    }
