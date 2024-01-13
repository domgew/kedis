package com.github.domgew.kedis.utils

import com.github.domgew.kedis.getEnv

object TestConfigUtil {
    fun getPort(): Int {
        return getEnv("REDIS_PORT")
            ?.toIntOrNull()
            ?: 6379
    }
}
