package com.github.domgew.kredis

public sealed interface KredisException {
    public data object ConnectionTimeout : Exception("Connection timed out"), KredisException
    public class WrongResponse(
        message: String,
    ) : Exception("Wrong response: $message"), KredisException
}
