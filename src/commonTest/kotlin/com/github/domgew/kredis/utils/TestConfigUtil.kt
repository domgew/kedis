package com.github.domgew.kredis.utils

import com.github.domgew.kredis.getProperty

object TestConfigUtil {
    fun getPort(): Int {
        return getProperty("REDIS_PORT")
            ?.toIntOrNull()
            ?: 6379
    }
}
