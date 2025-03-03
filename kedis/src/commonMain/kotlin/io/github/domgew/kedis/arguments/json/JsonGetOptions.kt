package io.github.domgew.kedis.arguments.json

import io.github.domgew.kedis.impl.RedisMessage

public class JsonGetOptions(
    private val indent: IndentOption? = null,
    private val newline: NewlineOption? = null,
    private val space: SpaceOption? = null,
) {

    internal fun toRedisMessages(): List<RedisMessage> {

        val result = arrayListOf<RedisMessage>()

        if (indent != null) {
            result.addAll(indent.toRedisMessages())
        }

        if (newline != null) {
            result.addAll(newline.toRedisMessages())
        }

        if (space != null) {
            result.addAll(space.toRedisMessages())
        }

        return result
    }

    public class IndentOption(private val indent: String) {

        internal fun toRedisMessages(): List<RedisMessage> {
            return listOf(
                RedisMessage.BulkStringMessage("INDENT"),
                RedisMessage.BulkStringMessage(indent),
            )
        }

    }

    public class NewlineOption(private val newline: String) {

        internal fun toRedisMessages(): List<RedisMessage> {
            return listOf(
                RedisMessage.BulkStringMessage("NEWLINE"),
                RedisMessage.BulkStringMessage(newline),
            )
        }

    }

    public class SpaceOption(private val space: String) {

        internal fun toRedisMessages(): List<RedisMessage> {
            return listOf(
                RedisMessage.BulkStringMessage("SPACE"),
                RedisMessage.BulkStringMessage(space),
            )
        }

    }

}
