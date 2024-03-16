package io.github.domgew.kedis.results.value

// https://redis.io/commands/ttl/
// https://redis.io/commands/pttl/
/**
 * How long the item still has to live.
 *
 * @see NotFound
 * @see Never
 * @see InSeconds
 * @see InMilliseconds
 */
public sealed interface TtlResult {

    public data object NotFound : TtlResult

    public data object Never : TtlResult

    public data class InSeconds(
        val seconds: Long,
    ) : TtlResult

    public data class InMilliseconds(
        val milliseconds: Long,
    ) : TtlResult
}
