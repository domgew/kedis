package io.github.domgew.kedis.results.value

// https://redis.io/commands/set/
public sealed interface SetResult {
    public val successful: Boolean
    public val written: Boolean

    public data object Aborted : SetResult {
        override val successful: Boolean = true
        override val written: Boolean = false
    }
    public data object Ok : SetResult {
        override val successful: Boolean = true
        override val written: Boolean = true
    }
    public data object NotFound : SetResult {
        override val successful: Boolean = true
        override val written: Boolean = true
    }
    public data class PreviousValue internal constructor(
        val data: String,
    ) : SetResult {
        override val successful: Boolean = true
        override val written: Boolean = true
    }
}
