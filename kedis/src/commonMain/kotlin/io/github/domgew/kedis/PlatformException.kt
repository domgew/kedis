package io.github.domgew.kedis

internal expect suspend fun <T> commoniseConnectException(
    block: suspend () -> T,
): T

internal expect suspend fun <T> commoniseNetworkExceptions(
    block: suspend () -> T,
): T
