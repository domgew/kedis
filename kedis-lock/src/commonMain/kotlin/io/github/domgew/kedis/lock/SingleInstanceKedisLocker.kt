package io.github.domgew.kedis.lock

import io.github.domgew.kedis.KedisClient
import io.github.domgew.kedis.arguments.value.SetOptions
import io.github.domgew.kedis.commands.KedisScriptingCommands
import io.github.domgew.kedis.commands.KedisValueCommands
import io.github.domgew.kedis.results.scripting.DynamicResult
import io.github.domgew.kedis.results.value.SetResult
import kotlin.time.Duration

public class SingleInstanceKedisLocker(
    private val kedisClient: KedisClient,
) {

    public suspend fun isLocked(
        key: String,
    ): Boolean =
        kedisClient.execute(
            command = KedisValueCommands.exists(
                key,
            ),
        ) > 0

    public suspend fun isLockedBy(
        key: String,
        lockerId: String,
    ): Boolean =
        kedisClient.execute(
            command = KedisValueCommands.get(
                key = key,
            ),
        ) == lockerId

    public suspend fun tryLock(
        key: String,
        lockerId: String,
        maxLockFor: Duration,
    ): Boolean {
        val result = kedisClient.execute(
            command = KedisValueCommands.set(
                key = key,
                value = lockerId,
                options = SetOptions(
                    previousKeyHandling = SetOptions.PreviousKeyHandling.KEEP_IF_EXISTS,
                    getPreviousValue = true,
                    expire = SetOptions.ExpireOption.ExpiresInMilliseconds(
                        milliseconds = maxLockFor.inWholeMilliseconds,
                    ),
                ),
            ),
        )

        return when (result) {
            SetResult.Aborted ->
                false

            SetResult.NotFound ->
                true

            SetResult.Ok ->
                lockerId == "OK"

            is SetResult.PreviousValue ->
                lockerId == result.value
        }
    }

    public suspend fun tryRelease(
        key: String,
        lockerId: String,
    ): Boolean {
        val result = kedisClient.execute(
            command = KedisScriptingCommands.eval(
                script = """
                    local curVal = redis.call("GET", KEYS[1])
                    if curVal == nil then
                        return 1
                    elseif curVal == ARGV[1] then
                        return redis.call("UNLINK", KEYS[1])
                    else
                        return 1
                    end
                """
                    .trimIndent(),
                keys = listOf(
                    key,
                ),
                args = listOf(
                    lockerId,
                ),
            ),
        )

        return when (result) {
            is DynamicResult.LongResult ->
                result.value > 0

            is DynamicResult.BigIntegerResult ->
                result.value
                    .trim() != "0"

            is DynamicResult.ErrorResult,
            is DynamicResult.BinaryOrStringResult,
            is DynamicResult.BooleanResult,
            is DynamicResult.DoubleResult,
            is DynamicResult.ListLikeResult,
            is DynamicResult.MapResult,
            DynamicResult.NullResult ->
                false
        }
    }
}
