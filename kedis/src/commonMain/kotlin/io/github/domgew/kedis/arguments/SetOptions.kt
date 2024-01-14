package io.github.domgew.kedis.arguments

import io.github.domgew.kedis.impl.RedisMessage

public data class SetOptions(
    val previousKeyHandling: PreviousKeyHandling = PreviousKeyHandling.OVERRIDE,
    val getPreviousValue: Boolean = false,
    val expire: ExpireOption? = null,
) {
    internal fun toRedisMessages(): List<RedisMessage> {
        val result = ArrayList<RedisMessage>()

        previousKeyHandling.toRedisMessage()
            ?.let {
                result.add(it)
            }

        if (getPreviousValue) {
            result.add(
                RedisMessage.BulkStringMessage("GET"),
            )
        }

        expire?.toRedisMessages()
            ?.let {
                result.addAll(it)
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

    public sealed class ExpireOption {
        protected abstract val paramName: String
        protected abstract val paramValue: Long?

        internal fun toRedisMessages(): List<RedisMessage> =
            paramValue
                ?.let {
                    listOf(
                        RedisMessage.BulkStringMessage(paramName),
                        RedisMessage.IntegerMessage(it),
                    )
                }
                ?: listOf(
                    RedisMessage.BulkStringMessage(paramName),
                )

        public data class ExpiresInSeconds(
            val seconds: Long,
        ): ExpireOption() {
            override val paramName: String = "EX"
            override val paramValue: Long
                get() = seconds
        }
        public data class ExpiresInMilliseconds(
            val milliseconds: Long,
        ): ExpireOption() {
            override val paramName: String = "PX"
            override val paramValue: Long
                get() = milliseconds
        }
        public data class ExpiresAtUnixEpochSecond(
            val unixEpochSecond: Long,
        ): ExpireOption() {
            override val paramName: String = "EXAT"
            override val paramValue: Long
                get() = unixEpochSecond
        }
        public data class ExpiresAtUnixEpochMillisecond(
            val unixEpochMillisecond: Long,
        ): ExpireOption() {
            override val paramName: String = "PXAT"
            override val paramValue: Long
                get() = unixEpochMillisecond
        }
        public data object KeepPreviousTTL: ExpireOption() {
            override val paramName: String = "KEEPTTL"
            override val paramValue: Long? = null
        }
    }
}