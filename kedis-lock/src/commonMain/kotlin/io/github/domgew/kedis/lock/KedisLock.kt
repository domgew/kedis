package io.github.domgew.kedis.lock

import kotlin.time.Duration

public interface KedisLock {

    public val key: String

    public suspend fun isLocked(): Boolean

    public suspend fun isLockedBy(
        lockerId: String,
    ): Boolean

    public suspend fun tryLock(
        lockerId: String,
        maxLockFor: Duration,
    ): Boolean

    public suspend fun tryRelease(
        lockerId: String,
    ): Boolean
}
