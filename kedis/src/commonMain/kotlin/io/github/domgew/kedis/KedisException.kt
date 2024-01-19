package io.github.domgew.kedis

public sealed interface KedisException {
    public data object ConnectionTimeout : Exception("Connection timed out"), KedisException
    public class WrongResponse(
        message: String,
    ) : Exception("Wrong response: $message"), KedisException
    public class RedisErrorResponse(
        message: String,
    ) : Exception("Redis responded with error: $message"), KedisException
}
