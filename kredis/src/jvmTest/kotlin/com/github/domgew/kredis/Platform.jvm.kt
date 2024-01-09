package com.github.domgew.kredis

internal actual fun getProperty(name: String): String? =
    System.getProperty(name)
