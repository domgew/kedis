package io.github.domgew.kedis.commands

import io.github.domgew.kedis.arguments.value.SetOptions
import io.github.domgew.kedis.commands.KedisValueCommands.decr
import io.github.domgew.kedis.commands.KedisValueCommands.decrBy
import io.github.domgew.kedis.commands.KedisValueCommands.getRange
import io.github.domgew.kedis.commands.KedisValueCommands.incr
import io.github.domgew.kedis.commands.KedisValueCommands.incrBy
import io.github.domgew.kedis.commands.KedisValueCommands.incrByFloat
import io.github.domgew.kedis.commands.value.AppendCommand
import io.github.domgew.kedis.commands.value.DecrByCommand
import io.github.domgew.kedis.commands.value.DecrCommand
import io.github.domgew.kedis.commands.value.DelCommand
import io.github.domgew.kedis.commands.value.ExistsCommand
import io.github.domgew.kedis.commands.value.ExpireTimeCommand
import io.github.domgew.kedis.commands.value.GetBinaryCommand
import io.github.domgew.kedis.commands.value.GetCommand
import io.github.domgew.kedis.commands.value.GetRangeCommand
import io.github.domgew.kedis.commands.value.IncrByCommand
import io.github.domgew.kedis.commands.value.IncrByFloatCommand
import io.github.domgew.kedis.commands.value.IncrCommand
import io.github.domgew.kedis.commands.value.SetBinaryCommand
import io.github.domgew.kedis.commands.value.SetCommand
import io.github.domgew.kedis.commands.value.StrLenCommand
import io.github.domgew.kedis.commands.value.TtlCommand
import io.github.domgew.kedis.commands.value.UnlinkCommand
import io.github.domgew.kedis.results.value.ExpireTimeResult
import io.github.domgew.kedis.results.value.SetBinaryResult
import io.github.domgew.kedis.results.value.SetResult
import io.github.domgew.kedis.results.value.TtlResult

public object KedisValueCommands {

    /**
     * Appends the given [value] to the current value behind the given [key]. If the [key] does not exist yet, it will be created.
     *
     * [https://redis.io/commands/append/](https://redis.io/commands/append/)
     * @return The length of the value after appending
     */
    public fun append(
        key: String,
        value: String,
    ): KedisCommand<Long> =
        AppendCommand(
            key = key,
            value = value,
        )

    /**
     * Decrements the value behind the given [key] by one (1). If it does not exist at the beginning, it is assumed to be 0 before decrementing.
     *
     * [https://redis.io/commands/decr/](https://redis.io/commands/decr/)
     * @return The value after decrementing
     * @see decrBy
     * @see incr
     * @see incrBy
     * @see incrByFloat
     */
    public fun decr(
        key: String,
    ): KedisCommand<Long> =
        DecrCommand(
            key = key,
        )

    /**
     * Decrements the value behind the given [key] by [by]. If it does not exist at the beginning, it is assumed to be 0 before decrementing.
     *
     * [https://redis.io/commands/decrby/](https://redis.io/commands/decrby/)
     * @return The value after decrementing
     * @see decr
     * @see incr
     * @see incrBy
     * @see incrByFloat
     */
    public fun decrBy(
        key: String,
        by: Long,
    ): KedisCommand<Long> =
        DecrByCommand(
            key = key,
            by = by,
        )

    /**
     * Removes the provided [key]s. If a key does not exist, no error is thrown.
     *
     * [https://redis.io/commands/del/](https://redis.io/commands/del/)
     * @return The number of removed provided [key]s
     */
    public fun del(
        vararg key: String,
    ): KedisCommand<Long> =
        DelCommand(
            keys = key.asList(),
        )

    /**
     * Checks whether the given [key]s exist.
     *
     * [https://redis.io/commands/exists/](https://redis.io/commands/exists/)
     * @return The number of provided [key]s that do exist
     */
    public fun exists(
        vararg key: String,
    ): KedisCommand<Long> =
        ExistsCommand(
            keys = key.asList(),
        )

    /**
     * Gets the time in UNIX seconds or milliseconds - depending on the [inMilliseconds] argument - when the given [key] expires.
     *
     * Only available for redis >=7.0.0.
     *
     * [https://redis.io/commands/expiretime/](https://redis.io/commands/expiretime/)
     *
     * [https://redis.io/commands/pexpiretime/](https://redis.io/commands/pexpiretime/)
     * @param inMilliseconds Whether the resulting time should be in milliseconds or seconds
     * @return The time UNIX timestamp ([inMilliseconds]) of expiration
     */
    public fun expireTime(
        key: String,
        inMilliseconds: Boolean = true,
    ): KedisCommand<ExpireTimeResult> =
        ExpireTimeCommand(
            key = key,
            inMilliseconds = inMilliseconds,
        )

    /**
     * Gets the value behind the given [key].
     *
     * [https://redis.io/commands/get/](https://redis.io/commands/get/)
     * @return The value or NULL
     */
    public fun get(
        key: String,
    ): KedisCommand<String?> =
        GetCommand(
            key = key,
        )

    /**
     * Gets the value behind the given [key].
     *
     * [https://redis.io/commands/get/](https://redis.io/commands/get/)
     * @return The value or NULL
     */
    public fun getBinary(
        key: String,
    ): KedisCommand<ByteArray?> =
        GetBinaryCommand(
            key = key,
        )

    /**
     * Gets part ([start]..[end] - both inclusive, clamped to real bounds) of the value behind the given [key]. The range parameters may also be negative to index from the end of the string. If the [key] does not exist, the result will be empty.
     *
     * [https://redis.io/commands/getrange/](https://redis.io/commands/getrange/)
     * @param start The inclusive start of the requested range - may be negative
     * @param end The inclusive end of the requested range - may be negative
     * @return The requested part ([start]..[end]) of the value behind the [key]
     * @see [getRange]
     */
    public fun getRange(
        key: String,
        start: Long,
        end: Long,
    ): KedisCommand<String> =
        GetRangeCommand(
            key = key,
            start = start,
            end = end,
        )

    /**
     * Gets part ([start]..[end] - both inclusive, clamped to real bounds) of the value behind the given [key]. The range parameters may also be negative to index from the end of the string. If the [key] does not exist, the result will be empty.
     *
     * [https://redis.io/commands/getrange/](https://redis.io/commands/getrange/)
     * @param start The inclusive start of the requested range - may be negative
     * @param end The inclusive end of the requested range - may be negative
     * @return The requested part ([start]..[end]) of the value behind the [key]
     * @see [getRange]
     */
    public fun getRange(
        key: String,
        range: LongRange,
    ): KedisCommand<String> =
        getRange(
            key = key,
            start = range.first,
            end = range.last,
        )

    /**
     * Increments the value behind the given [key] by one (1). If it does not exist at the beginning, it is assumed to be 0 before incrementing.
     *
     * [https://redis.io/commands/incr/](https://redis.io/commands/incr/)
     * @return The value after incrementing
     * @see incrBy
     * @see decr
     * @see decrBy
     * @see incrByFloat
     */
    public fun incr(
        key: String,
    ): KedisCommand<Long> =
        IncrCommand(
            key = key,
        )

    /**
     * Increments the value behind the given [key] by [by]. If it does not exist at the beginning, it is assumed to be 0 before incrementing.
     *
     * [https://redis.io/commands/incrby/](https://redis.io/commands/incrby/)
     * @return The value after incrementing
     * @see incr
     * @see decr
     * @see decrBy
     * @see incrByFloat
     */
    public fun incrBy(
        key: String,
        by: Long,
    ): KedisCommand<Long> =
        IncrByCommand(
            key = key,
            by = by,
        )

    /**
     * Increments (or decrements when [by] is negative) the value behind the given [key] by [by]. If it does not exist at the beginning, it is assumed to be 0 before incrementing / decrementing.
     *
     * [https://redis.io/commands/incrbyfloat/](https://redis.io/commands/incrbyfloat/)
     * @return The value after incrementing / decrementing
     */
    public fun incrByFloat(
        key: String,
        by: Double,
    ): KedisCommand<Double> =
        IncrByFloatCommand(
            key = key,
            by = by,
        )

    /**
     * Sets the value behind the given [key], minding the [options].
     *
     * [https://redis.io/commands/set/](https://redis.io/commands/set/)
     * @return Whether the operation was successful and the previous value if requested
     */
    public fun set(
        key: String,
        value: String,
        options: SetOptions = SetOptions(),
    ): KedisCommand<SetResult> =
        SetCommand(
            key = key,
            value = value,
            options = options,
        )

    /**
     * Sets the value behind the given [key], minding the [options].
     *
     * [https://redis.io/commands/set/](https://redis.io/commands/set/)
     * @return Whether the operation was successful and the previous value if requested
     */
    public fun setBinary(
        key: String,
        value: ByteArray,
        options: SetOptions = SetOptions(),
    ): KedisCommand<SetBinaryResult> =
        SetBinaryCommand(
            key = key,
            value = value,
            options = options,
        )

    /**
     * Retrieves the string length of the value behind the given [key]. If the [key] does not exist, it will be 0.
     *
     * Only works on string values. It may or may not work on binary data.
     *
     * [https://redis.io/commands/strlen/](https://redis.io/commands/strlen/)
     * @return The length of the value
     */
    public fun strLen(
        key: String,
    ): KedisCommand<Long> =
        StrLenCommand(
            key = key,
        )

    /**
     * Gets the remaining time-to-live in seconds or milliseconds - depending on the [inMilliseconds] argument.
     *
     * [https://redis.io/commands/ttl/](https://redis.io/commands/ttl/)
     *
     * [https://redis.io/commands/pttl/](https://redis.io/commands/pttl/)
     * @param inMilliseconds Whether the resulting time should be in milliseconds or seconds
     * @return The remaining time-to-live (seconds or milliseconds)
     */
    public fun ttl(
        key: String,
        inMilliseconds: Boolean = true,
    ): KedisCommand<TtlResult> =
        TtlCommand(
            key = key,
            inMilliseconds = inMilliseconds,
        )

    /**
     * Unlinks the provided [key]s. If a key does not exist, no error is thrown.
     *
     * [https://redis.io/commands/unlink/](https://redis.io/commands/unlink/)
     * @return The number of unlinked provided [key]s
     */
    public fun unlink(
        vararg key: String,
    ): KedisCommand<Long> =
        UnlinkCommand(
            keys = key.asList(),
        )
}
