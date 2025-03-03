package io.github.domgew.kedis.arguments.json

import io.github.domgew.kedis.impl.RedisMessage

public data class JsonSetOptions(
    val previousKeyHandling: PreviousKeyHandling = PreviousKeyHandling.OVERRIDE,
) {
    internal fun toRedisMessages(): List<RedisMessage> {
        val result = ArrayList<RedisMessage>()

        previousKeyHandling.toRedisMessage()
            ?.let {
                result.add(it)
            }

        return result
    }

    public enum class PreviousKeyHandling(
        internal val apiValue: String?,
    ) {
        /**
         * Set the given key to the given value in any case
         */
        OVERRIDE(null),

        /**
         * Set the given key to the given value only if the key did not already exist.
         */
        KEEP_IF_EXISTS("NX"),

        /**
         * Set the given key to the given value only if the key did already exist.
         */
        OVERRIDE_ONLY("XX"),
        ;

        internal fun toRedisMessage(): RedisMessage? =
            apiValue
                ?.let {
                    RedisMessage.BulkStringMessage(it)
                }
    }
}
