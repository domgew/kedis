package io.github.domgew.kedis.lock.utils

internal expect fun getEnv(
    name: String,
): String?
