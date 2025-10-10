package io.github.domgew.kedis.lock.internal

import io.github.domgew.kedis.lock.KedisDynamicLock
import io.github.domgew.kedis.lock.SingleInstanceKedisLocker
import kotlin.time.Duration

internal class SingleInstanceKedisDynamicLock(
    private val singleInstanceKedisLocker: SingleInstanceKedisLocker,
) : KedisDynamicLock {

    override suspend fun isLocked(
        key: String,
    ): Boolean =
        singleInstanceKedisLocker.isLocked(
            key = key,
        )

    override suspend fun isLockedBy(
        key: String,
        lockerId: String,
    ): Boolean =
        singleInstanceKedisLocker.isLockedBy(
            key = key,
            lockerId = lockerId,
        )

    override suspend fun tryLock(
        key: String,
        lockerId: String,
        maxLockFor: Duration,
    ): Boolean =
        singleInstanceKedisLocker.tryLock(
            key = key,
            lockerId = lockerId,
            maxLockFor = maxLockFor,
        )

    override suspend fun tryRelease(
        key: String,
        lockerId: String,
    ): Boolean =
        singleInstanceKedisLocker.tryRelease(
            key = key,
            lockerId = lockerId,
        )
}
