package com.github.domgew.kredis

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.toKString
import platform.posix.getenv

@OptIn(ExperimentalForeignApi::class)
internal actual fun getProperty(name: String): String? =
    getenv(name)?.toKString()
