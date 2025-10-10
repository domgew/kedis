package io.github.domgew.kedis.lock

import kotlin.time.Duration

public interface KedisDynamicLock {

    public suspend fun isLocked(
        key: String,
    ): Boolean

    public suspend fun isLockedBy(
        key: String,
        lockerId: String,
    ): Boolean

    public suspend fun tryLock(
        key: String,
        lockerId: String,
        maxLockFor: Duration,
    ): Boolean

    public suspend fun tryRelease(
        key: String,
        lockerId: String,
    ): Boolean
}
