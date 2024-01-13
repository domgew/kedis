package com.github.domgew.kedis

internal actual fun getEnv(name: String): String? =
    System.getenv(name)
