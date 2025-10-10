package io.github.domgew.kedis.lock.utils

internal actual fun getEnv(
    name: String,
): String? =
    System.getenv(name)
