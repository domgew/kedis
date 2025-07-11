package io.github.domgew.kedis.annotations

@RequiresOptIn(
    message = "The JSON module for Redis is required",
    level = RequiresOptIn.Level.ERROR,
)
public annotation class RedisModuleJson
