package io.github.domgew.kedis.lock.utils

object TestConfigUtil {

    fun getPort(): Int {
        return getEnv("REDIS_PORT")
            ?.toIntOrNull()
            ?: 6379
    }
}
