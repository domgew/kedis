package io.github.domgew.kedis.lock

import kotlin.time.Duration
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.withContext

public suspend inline fun <T> KedisLock.withTryLock(
    maxLockFor: Duration,
    default: suspend () -> T,
    block: suspend () -> T,
): T {
    @OptIn(ExperimentalUuidApi::class)
    val id = Uuid.random()
        .toString()

    val acquired = try {
        tryLock(
            lockerId = id,
            maxLockFor = maxLockFor,
        )
    } catch (_: Exception) {
        false
    }

    if (!acquired) {
        return default()
    }

    try {
        return block()
    } finally {
        withContext(NonCancellable) {
            tryRelease(
                lockerId = id,
            )
        }
    }
}
