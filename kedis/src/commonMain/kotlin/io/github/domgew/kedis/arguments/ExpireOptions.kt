package io.github.domgew.kedis.arguments

import io.github.domgew.kedis.impl.RedisMessage

public data class ExpireOptions(
    val previousKeyHandling: PreviousKeyHandling? = null,
) {
    internal fun toRedisMessages(): List<RedisMessage> {
        val result = ArrayList<RedisMessage>()

        previousKeyHandling?.toRedisMessage()
            ?.let {
                result.add(it)
            }

        return result
    }

    public enum class PreviousKeyHandling(
        internal val apiValue: String,
    ) {

        /**
         * Set expiry only when the key has no expiry
         */
        KEEP_IF_EXISTS("NX"),

        /**
         * Set expiry only when the key has an existing expiry
         */
        OVERRIDE_ONLY("XX"),

        /**
         * Set expiry only when the new expiry is greater than current one
         */
        GREATER_THAN("GT"),

        /**
         * Set expiry only when the new expiry is less than current one
         */
        LOWER_THAN("GT"),

        ;

        internal fun toRedisMessage(): RedisMessage? =
            apiValue
                ?.let {
                    RedisMessage.BulkStringMessage(it)
                }
    }

}
