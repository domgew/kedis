package io.github.domgew.kedis

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.toKString
import platform.posix.getenv

@OptIn(ExperimentalForeignApi::class)
internal actual fun getEnv(name: String): String? =
    getenv(name)?.toKString()
