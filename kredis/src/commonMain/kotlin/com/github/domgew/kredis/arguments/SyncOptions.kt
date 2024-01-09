package com.github.domgew.kredis.arguments

public enum class SyncOptions {
    SYNC,
    ASYNC,
    ;

    override fun toString(): String =
        this.name
}
