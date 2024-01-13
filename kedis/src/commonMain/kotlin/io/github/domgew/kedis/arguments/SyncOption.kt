package io.github.domgew.kedis.arguments

public enum class SyncOption {
    SYNC,
    ASYNC,
    ;

    override fun toString(): String =
        this.name
}
