package io.github.domgew.kedis.utils

import io.github.domgew.kedis.getEnv

object TestConfigUtil {
    fun getPort(): Int {
        return getEnv("REDIS_PORT")
            ?.toIntOrNull()
            ?: 6379
    }
}
