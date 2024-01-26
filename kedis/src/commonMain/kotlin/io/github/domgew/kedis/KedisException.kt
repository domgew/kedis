package io.github.domgew.kedis

public sealed class KedisException private constructor(
    message: String,
    cause: Throwable? = null,
) : Exception(
    /* message = */ message,
    /* cause = */ cause,
) {
    public class ConnectException(
        cause: Throwable,
    ) : KedisException(
        message = "Could not connect: ${cause.message ?: "--"}",
        cause = cause,
    )

    public class ConnectionTimeoutException(
    ) : KedisException(
        message = "Connection timed out",
    )

    public class WrongResponseException(
        message: String,
    ) : KedisException(
        message = "Wrong response: $message",
    )

    public class RedisErrorResponseException(
        message: String,
    ) : KedisException(
        message = "Redis responded with error: $message",
    )
}
