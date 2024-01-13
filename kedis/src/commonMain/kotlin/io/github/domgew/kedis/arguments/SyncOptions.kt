package io.github.domgew.kedis.arguments

public enum class SyncOptions {
    SYNC,
    ASYNC,
    ;

    override fun toString(): String =
        this.name
}
