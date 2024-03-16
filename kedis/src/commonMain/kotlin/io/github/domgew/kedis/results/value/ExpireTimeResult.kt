package io.github.domgew.kedis.results.value

// https://redis.io/commands/expiretime/
// https://redis.io/commands/pexpiretime/
/**
 * When the item expires.
 *
 * @see NotFound
 * @see Never
 * @see AtUnixSecond
 * @see AtUnixMillisecond
 */
public sealed interface ExpireTimeResult {

    public data object NotFound : ExpireTimeResult

    public data object Never : ExpireTimeResult

    public data class AtUnixSecond(
        val seconds: Long,
    ) : ExpireTimeResult

    public data class AtUnixMillisecond(
        val milliseconds: Long,
    ) : ExpireTimeResult
}
