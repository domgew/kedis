package io.github.domgew.kedis.commands.json

import io.github.domgew.kedis.KedisException
import io.github.domgew.kedis.commands.KedisFullCommand
import io.github.domgew.kedis.impl.RedisMessage

// // see https://redis.io/commands/json.get/
internal class JsonGetCommand(
    val key: String,
    val indent: String? = null,
    val newLine: String? = null,
    val space: String? = null,
    val paths: List<String> = emptyList(),
) : KedisFullCommand<String?> {

    override fun fromRedisResponse(
        response: RedisMessage,
    ): String? =
        when (response) {
            is RedisMessage.StringMessage ->
                response.value

            is RedisMessage.NullMessage ->
                null

            is RedisMessage.ErrorMessage ->
                handleRedisErrorResponse(
                    response = response,
                )

            else ->
                throw KedisException.WrongResponseException(
                    message = "Expected string or null response, was ${response::class.simpleName}",
                )
        }

    override fun toRedisRequest(): RedisMessage =
        RedisMessage.ArrayMessage(
            value = listOf(
                RedisMessage.BulkStringMessage(OPERATION_NAME),
                RedisMessage.BulkStringMessage(key),
                *indentParam(),
                *newLineParam(),
                *spaceParam(),
                *pathParams(),
            ),
        )

    private fun indentParam(): Array<RedisMessage> =
        indent
            ?.let {
                arrayOf(
                    RedisMessage.BulkStringMessage(PARAM_INDENT),
                    RedisMessage.BulkStringMessage(it),
                )
            }
            ?: emptyArray()

    private fun newLineParam(): Array<RedisMessage> =
        newLine
            ?.let {
                arrayOf(
                    RedisMessage.BulkStringMessage(PARAM_NEW_LINE),
                    RedisMessage.BulkStringMessage(it),
                )
            }
            ?: emptyArray()

    private fun spaceParam(): Array<RedisMessage> =
        space
            ?.let {
                arrayOf(
                    RedisMessage.BulkStringMessage(PARAM_SPACE),
                    RedisMessage.BulkStringMessage(it),
                )
            }
            ?: emptyArray()

    private fun pathParams(): Array<RedisMessage> =
        paths
            .map {
                RedisMessage.BulkStringMessage(it)
            }
            .toTypedArray()

    companion object {
        private const val OPERATION_NAME = "JSON.GET"
        private const val PARAM_INDENT = "INDENT"
        private const val PARAM_NEW_LINE = "NEWLINE"
        private const val PARAM_SPACE = "SPACE"
    }
}
