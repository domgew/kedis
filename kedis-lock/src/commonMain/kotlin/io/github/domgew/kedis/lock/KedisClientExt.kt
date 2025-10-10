package io.github.domgew.kedis.lock

import io.github.domgew.kedis.KedisClient

public fun KedisClient.locker(): SingleInstanceKedisLocker =
    SingleInstanceKedisLocker(
        kedisClient = this,
    )
