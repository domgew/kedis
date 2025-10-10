package io.github.domgew.kedis.lock

import io.github.domgew.kedis.lock.internal.SingleInstanceKedisDynamicLock
import io.github.domgew.kedis.lock.internal.SingleInstanceKedisLock

public fun SingleInstanceKedisLocker.asDynamicLock(): KedisDynamicLock =
    SingleInstanceKedisDynamicLock(
        singleInstanceKedisLocker = this,
    )

public fun SingleInstanceKedisLocker.asLock(
    key: String,
): KedisLock =
    SingleInstanceKedisLock(
        singleInstanceKedisLocker = this,
        key = key,
    )
