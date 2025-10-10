package io.github.domgew.kedis.lock.internal

import io.github.domgew.kedis.lock.KedisLock
import io.github.domgew.kedis.lock.SingleInstanceKedisLocker
import kotlin.time.Duration

internal class SingleInstanceKedisLock(
    private val singleInstanceKedisLocker: SingleInstanceKedisLocker,
    override val key: String,
) : KedisLock {

    override suspend fun isLocked(): Boolean =
        singleInstanceKedisLocker.isLocked(
            key = key,
        )

    override suspend fun isLockedBy(
        lockerId: String,
    ): Boolean =
        singleInstanceKedisLocker.isLockedBy(
            key = key,
            lockerId = lockerId,
        )

    override suspend fun tryLock(
        lockerId: String,
        maxLockFor: Duration,
    ): Boolean =
        singleInstanceKedisLocker.tryLock(
            key = key,
            lockerId = lockerId,
            maxLockFor = maxLockFor,
        )

    override suspend fun tryRelease(
        lockerId: String,
    ): Boolean =
        singleInstanceKedisLocker.tryRelease(
            key = key,
            lockerId = lockerId,
        )
}
