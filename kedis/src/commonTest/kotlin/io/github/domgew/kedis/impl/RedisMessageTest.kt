package io.github.domgew.kedis.impl

import com.ionspin.kotlin.bignum.integer.BigInteger
import io.github.domgew.kedis.utils.TestByteWriteChannel
import io.ktor.utils.io.ByteReadChannel
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlinx.coroutines.test.runTest

// see https://redis.io/docs/reference/protocol-spec/
class RedisMessageTest {

    @Test
    fun simpleStringMessage() = runTest {
        testEncodeDecoding(
            expectedEncoded = "+OK\r\n",
            expected = RedisMessage.SimpleStringMessage(
                value = "OK",
            ),
        )
        val exception = assertFailsWith<RedisMessage.ParsingException> {
            decodeFromString("+OK\r+OK\r\n")
        }
        assertContains(
            exception.message
                ?: "",
            "LF",
        )
    }

    @Test
    fun simpleErrorMessage() = runTest {
        testEncodeDecoding(
            expectedEncoded = "-Error message\r\n",
            expected = RedisMessage.SimpleErrorMessage(
                value = "Error message",
            ),
        )
    }

    @Test
    fun integerMessage() = runTest {
        // :[<+|->]<value>
        testEncodeDecoding(
            expectedEncoded = ":0\r\n",
            expected = RedisMessage.IntegerMessage(
                value = 0,
            ),
        )
        testEncodeDecoding(
            expectedEncoded = ":10\r\n",
            expected = RedisMessage.IntegerMessage(
                value = 10,
            ),
        )
        // no re-encoding since the plus is optional
        testDecoding(
            encodedInput = ":+10\r\n",
            expected = RedisMessage.IntegerMessage(
                value = 10,
            ),
        )
        testEncodeDecoding(
            expectedEncoded = ":-10\r\n",
            expected = RedisMessage.IntegerMessage(
                value = -10,
            ),
        )
    }

    @Test
    fun bulkStringMessage() = runTest {
        testEncodeDecoding(
            expectedEncoded = "$2\r\nOK\r\n",
            expected = RedisMessage.BulkStringMessage(
                value = "OK",
            ),
        )
        testEncodeDecoding(
            expectedEncoded = "$7\r\nOK\r\nOK2\r\n",
            expected = RedisMessage.BulkStringMessage(
                value = "OK\r\nOK2",
            ),
        )
        testEncodeDecoding(
            expectedEncoded = "$0\r\n\r\n",
            expected = RedisMessage.BulkStringMessage(
                value = "",
            ),
        )
        testDecoding(
            encodedInput = "$-1\r\n",
            expected = RedisMessage.NullMessage,
        )
        val exception = assertFailsWith<RedisMessage.ParsingException> {
            decodeFromString("$1\r\na\n\r")
        }
        assertContains(
            exception.message
                ?: "",
            "CR",
        )
    }

    @Test
    fun arrayMessage() = runTest {
        testEncodeDecoding(
            expectedEncoded = "*0\r\n",
            expected = RedisMessage.ArrayMessage(
                value = emptyList(),
            ),
        )
        testEncodeDecoding(
            expectedEncoded = "*2\r\n$5\r\nhello\r\n$5\r\nworld\r\n",
            expected = RedisMessage.ArrayMessage(
                value = listOf(
                    RedisMessage.BulkStringMessage(
                        value = "hello",
                    ),
                    RedisMessage.BulkStringMessage(
                        value = "world",
                    ),
                ),
            ),
        )
        testEncodeDecoding(
            expectedEncoded = "*3\r\n:1\r\n:2\r\n:3\r\n",
            expected = RedisMessage.ArrayMessage(
                value = listOf(
                    RedisMessage.IntegerMessage(
                        value = 1,
                    ),
                    RedisMessage.IntegerMessage(
                        value = 2,
                    ),
                    RedisMessage.IntegerMessage(
                        value = 3,
                    ),
                ),
            ),
        )
        testEncodeDecoding(
            expectedEncoded = "*5\r\n:1\r\n:2\r\n:3\r\n:4\r\n$5\r\nhello\r\n",
            expected = RedisMessage.ArrayMessage(
                value = listOf(
                    RedisMessage.IntegerMessage(
                        value = 1,
                    ),
                    RedisMessage.IntegerMessage(
                        value = 2,
                    ),
                    RedisMessage.IntegerMessage(
                        value = 3,
                    ),
                    RedisMessage.IntegerMessage(
                        value = 4,
                    ),
                    RedisMessage.BulkStringMessage(
                        value = "hello",
                    ),
                ),
            ),
        )
        testEncodeDecoding(
            expectedEncoded = "*2\r\n*3\r\n:1\r\n:2\r\n:3\r\n*2\r\n+Hello\r\n-World\r\n",
            expected = RedisMessage.ArrayMessage(
                value = listOf(
                    RedisMessage.ArrayMessage(
                        value = listOf(
                            RedisMessage.IntegerMessage(
                                value = 1,
                            ),
                            RedisMessage.IntegerMessage(
                                value = 2,
                            ),
                            RedisMessage.IntegerMessage(
                                value = 3,
                            ),
                        ),
                    ),
                    RedisMessage.ArrayMessage(
                        value = listOf(
                            RedisMessage.SimpleStringMessage(
                                value = "Hello",
                            ),
                            RedisMessage.SimpleErrorMessage(
                                value = "World",
                            ),
                        ),
                    ),
                ),
            ),
        )
        // null will be encoded differently
        testDecoding(
            encodedInput = "*-1\r\n",
            expected = RedisMessage.NullMessage,
        )
        testDecoding(
            encodedInput = "*3\r\n$5\r\nhello\r\n$-1\r\n$5\r\nworld\r\n",
            expected = RedisMessage.ArrayMessage(
                value = listOf(
                    RedisMessage.BulkStringMessage(
                        value = "hello",
                    ),
                    RedisMessage.NullMessage,
                    RedisMessage.BulkStringMessage(
                        value = "world",
                    ),
                ),
            ),
        )
    }

    @Test
    fun nullMessage() = runTest {
        val decoded = testDecoding(
            encodedInput = "_\r\n",
            expected = RedisMessage.NullMessage,
        )
        testDecoding(
            encodedInput = encodeToString(decoded),
            expected = RedisMessage.NullMessage,
        )
    }

    @Test
    fun booleanMessage() = runTest {
        testEncodeDecoding(
            expectedEncoded = "#t\r\n",
            expected = RedisMessage.BooleanMessage(
                value = true,
            ),
        )
        testEncodeDecoding(
            expectedEncoded = "#f\r\n",
            expected = RedisMessage.BooleanMessage(
                value = false,
            ),
        )
        assertContains(
            assertFailsWith<RedisMessage.ParsingException> {
                decodeFromString("#a\r\n")
            }
                .message
                ?: "",
            RedisMessage.BooleanMessage::class
                .simpleName
                ?: "-!-",
        )
    }

    @Test
    fun doubleMessage() = runTest {
        testEncodeDecoding(
            expectedEncoded = ",1.23\r\n",
            expected = RedisMessage.DoubleMessage(
                value = 1.23,
            ),
        )
        // encoding introduces additional zero value decimal places
        testDecoding(
            encodedInput = ",10\r\n",
            expected = RedisMessage.DoubleMessage(
                value = 10.0,
            ),
        )
        testEncodeDecoding(
            expectedEncoded = ",inf\r\n",
            expected = RedisMessage.DoubleMessage(
                value = Double.POSITIVE_INFINITY,
            ),
        )
        testEncodeDecoding(
            expectedEncoded = ",-inf\r\n",
            expected = RedisMessage.DoubleMessage(
                value = Double.NEGATIVE_INFINITY,
            ),
        )
        testEncodeDecoding(
            expectedEncoded = ",nan\r\n",
            expected = RedisMessage.DoubleMessage(
                value = Double.NaN,
            ),
        )
        testEncodeDecoding(
            expectedEncoded = ",0.5\r\n",
            expected = RedisMessage.DoubleMessage(
                value = 0.5,
            ),
        )
        testEncodeDecoding(
            expectedEncoded = ",-0.5\r\n",
            expected = RedisMessage.DoubleMessage(
                value = -0.5,
            ),
        )
        testDecoding(
            encodedInput = ",+0.5\r\n",
            expected = RedisMessage.DoubleMessage(
                value = 0.5,
            ),
        )
        testDecoding(
            encodedInput = ",0.5e1\r\n",
            expected = RedisMessage.DoubleMessage(
                value = 5.0,
            ),
        )
        testDecoding(
            encodedInput = ",+0.5e1\r\n",
            expected = RedisMessage.DoubleMessage(
                value = 5.0,
            ),
        )
        testDecoding(
            encodedInput = ",-0.5e1\r\n",
            expected = RedisMessage.DoubleMessage(
                value = -5.0,
            ),
        )
        testDecoding(
            encodedInput = ",0.5e-1\r\n",
            expected = RedisMessage.DoubleMessage(
                value = 0.05,
            ),
        )
        testDecoding(
            encodedInput = ",0.5E1\r\n",
            expected = RedisMessage.DoubleMessage(
                value = 5.0,
            ),
        )
        testDecoding(
            encodedInput = ",+0.5E1\r\n",
            expected = RedisMessage.DoubleMessage(
                value = 5.0,
            ),
        )
        testDecoding(
            encodedInput = ",-0.5E1\r\n",
            expected = RedisMessage.DoubleMessage(
                value = -5.0,
            ),
        )
        testDecoding(
            encodedInput = ",0.5E-1\r\n",
            expected = RedisMessage.DoubleMessage(
                value = 0.05,
            ),
        )
    }

    @Test
    fun bigNumberMessage() = runTest {
        testEncodeDecoding(
            expectedEncoded = "(0\r\n",
            expected = RedisMessage.BigNumberMessage(
                value = BigInteger.parseString(
                    "0",
                ),
            ),
        )
        testEncodeDecoding(
            expectedEncoded = "(1\r\n",
            expected = RedisMessage.BigNumberMessage(
                value = BigInteger.parseString(
                    "1",
                ),
            ),
        )
        testEncodeDecoding(
            expectedEncoded = "(-1\r\n",
            expected = RedisMessage.BigNumberMessage(
                value = BigInteger.parseString(
                    "-1",
                ),
            ),
        )
        testDecoding(
            encodedInput = "(+1\r\n",
            expected = RedisMessage.BigNumberMessage(
                value = BigInteger.parseString(
                    "+1",
                ),
            ),
        )
        testEncodeDecoding(
            expectedEncoded = "(3492890328409238509324850943850943825024385\r\n",
            expected = RedisMessage.BigNumberMessage(
                value = BigInteger.parseString(
                    "3492890328409238509324850943850943825024385",
                ),
            ),
        )
        testEncodeDecoding(
            expectedEncoded = "(-3492890328409238509324850943850943825024385\r\n",
            expected = RedisMessage.BigNumberMessage(
                value = BigInteger.parseString(
                    "-3492890328409238509324850943850943825024385",
                ),
            ),
        )
        testDecoding(
            encodedInput = "(+3492890328409238509324850943850943825024385\r\n",
            expected = RedisMessage.BigNumberMessage(
                value = BigInteger.parseString(
                    "+3492890328409238509324850943850943825024385",
                ),
            ),
        )
    }

    @Test
    fun bulkErrorMessage() = runTest {
        testEncodeDecoding(
            expectedEncoded = "!2\r\nOK\r\n",
            expected = RedisMessage.BulkErrorMessage(
                value = "OK",
            ),
        )
        testEncodeDecoding(
            expectedEncoded = "!7\r\nOK\r\nOK2\r\n",
            expected = RedisMessage.BulkErrorMessage(
                value = "OK\r\nOK2",
            ),
        )
        testEncodeDecoding(
            expectedEncoded = "!0\r\n\r\n",
            expected = RedisMessage.BulkErrorMessage(
                value = "",
            ),
        )
        testEncodeDecoding(
            expectedEncoded = "!21\r\nSYNTAX invalid syntax\r\n",
            expected = RedisMessage.BulkErrorMessage(
                value = "SYNTAX invalid syntax",
            ),
        )
    }

    @Test
    fun verbatimStringMessage() = runTest {
        testEncodeDecoding(
            expectedEncoded = "=15\r\ntxt:Some string\r\n",
            expected = RedisMessage.VerbatimStringMessage(
                type = "txt",
                value = "Some string",
            ),
        )
    }

    @Test
    fun mapMessage() = runTest {
        testEncodeDecoding(
            expectedEncoded = "%2\r\n+first\r\n:1\r\n+second\r\n:2\r\n",
            expected = RedisMessage.MessageMapMessage(
                value = mapOf(
                    RedisMessage.SimpleStringMessage(
                        value = "first",
                    ) to RedisMessage.IntegerMessage(
                        value = 1,
                    ),
                    RedisMessage.SimpleStringMessage(
                        value = "second",
                    ) to RedisMessage.IntegerMessage(
                        value = 2,
                    ),
                ),
            ),
        )
        testEncodeDecoding(
            expectedEncoded = "%0\r\n",
            expected = RedisMessage.MessageMapMessage(
                value = emptyMap(),
            ),
        )
    }

    @Test
    fun setMessage() = runTest {
        testEncodeDecoding(
            expectedEncoded = "~0\r\n",
            expected = RedisMessage.MessageSetMessage(
                value = emptySet(),
            ),
        )
        testEncodeDecoding(
            expectedEncoded = "~2\r\n$5\r\nhello\r\n$5\r\nworld\r\n",
            expected = RedisMessage.MessageSetMessage(
                value = setOf(
                    RedisMessage.BulkStringMessage(
                        value = "hello",
                    ),
                    RedisMessage.BulkStringMessage(
                        value = "world",
                    ),
                ),
            ),
        )
        testEncodeDecoding(
            expectedEncoded = "~3\r\n:1\r\n:2\r\n:3\r\n",
            expected = RedisMessage.MessageSetMessage(
                value = setOf(
                    RedisMessage.IntegerMessage(
                        value = 1,
                    ),
                    RedisMessage.IntegerMessage(
                        value = 2,
                    ),
                    RedisMessage.IntegerMessage(
                        value = 3,
                    ),
                ),
            ),
        )
        testEncodeDecoding(
            expectedEncoded = "~5\r\n:1\r\n:2\r\n:3\r\n:4\r\n$5\r\nhello\r\n",
            expected = RedisMessage.MessageSetMessage(
                value = setOf(
                    RedisMessage.IntegerMessage(
                        value = 1,
                    ),
                    RedisMessage.IntegerMessage(
                        value = 2,
                    ),
                    RedisMessage.IntegerMessage(
                        value = 3,
                    ),
                    RedisMessage.IntegerMessage(
                        value = 4,
                    ),
                    RedisMessage.BulkStringMessage(
                        value = "hello",
                    ),
                ),
            ),
        )
        testEncodeDecoding(
            expectedEncoded = "~2\r\n~3\r\n:1\r\n:2\r\n:3\r\n~2\r\n+Hello\r\n-World\r\n",
            expected = RedisMessage.MessageSetMessage(
                value = setOf(
                    RedisMessage.MessageSetMessage(
                        value = setOf(
                            RedisMessage.IntegerMessage(
                                value = 1,
                            ),
                            RedisMessage.IntegerMessage(
                                value = 2,
                            ),
                            RedisMessage.IntegerMessage(
                                value = 3,
                            ),
                        ),
                    ),
                    RedisMessage.MessageSetMessage(
                        value = setOf(
                            RedisMessage.SimpleStringMessage(
                                value = "Hello",
                            ),
                            RedisMessage.SimpleErrorMessage(
                                value = "World",
                            ),
                        ),
                    ),
                ),
            ),
        )
        testDecoding(
            encodedInput = "~3\r\n$5\r\nhello\r\n$-1\r\n$5\r\nworld\r\n",
            expected = RedisMessage.MessageSetMessage(
                value = setOf(
                    RedisMessage.BulkStringMessage(
                        value = "hello",
                    ),
                    RedisMessage.NullMessage,
                    RedisMessage.BulkStringMessage(
                        value = "world",
                    ),
                ),
            ),
        )
    }

    @Test
    fun pushMessage() = runTest {
        testEncodeDecoding(
            expectedEncoded = ">0\r\n",
            expected = RedisMessage.MessagePushMessage(
                value = emptyList(),
            ),
        )
        testEncodeDecoding(
            expectedEncoded = ">2\r\n$5\r\nhello\r\n$5\r\nworld\r\n",
            expected = RedisMessage.MessagePushMessage(
                value = listOf(
                    RedisMessage.BulkStringMessage(
                        value = "hello",
                    ),
                    RedisMessage.BulkStringMessage(
                        value = "world",
                    ),
                ),
            ),
        )
        testEncodeDecoding(
            expectedEncoded = ">3\r\n:1\r\n:2\r\n:3\r\n",
            expected = RedisMessage.MessagePushMessage(
                value = listOf(
                    RedisMessage.IntegerMessage(
                        value = 1,
                    ),
                    RedisMessage.IntegerMessage(
                        value = 2,
                    ),
                    RedisMessage.IntegerMessage(
                        value = 3,
                    ),
                ),
            ),
        )
        testEncodeDecoding(
            expectedEncoded = ">5\r\n:1\r\n:2\r\n:3\r\n:4\r\n$5\r\nhello\r\n",
            expected = RedisMessage.MessagePushMessage(
                value = listOf(
                    RedisMessage.IntegerMessage(
                        value = 1,
                    ),
                    RedisMessage.IntegerMessage(
                        value = 2,
                    ),
                    RedisMessage.IntegerMessage(
                        value = 3,
                    ),
                    RedisMessage.IntegerMessage(
                        value = 4,
                    ),
                    RedisMessage.BulkStringMessage(
                        value = "hello",
                    ),
                ),
            ),
        )
        testEncodeDecoding(
            expectedEncoded = ">2\r\n*3\r\n:1\r\n:2\r\n:3\r\n*2\r\n+Hello\r\n-World\r\n",
            expected = RedisMessage.MessagePushMessage(
                value = listOf(
                    RedisMessage.ArrayMessage(
                        value = listOf(
                            RedisMessage.IntegerMessage(
                                value = 1,
                            ),
                            RedisMessage.IntegerMessage(
                                value = 2,
                            ),
                            RedisMessage.IntegerMessage(
                                value = 3,
                            ),
                        ),
                    ),
                    RedisMessage.ArrayMessage(
                        value = listOf(
                            RedisMessage.SimpleStringMessage(
                                value = "Hello",
                            ),
                            RedisMessage.SimpleErrorMessage(
                                value = "World",
                            ),
                        ),
                    ),
                ),
            ),
        )
        testDecoding(
            encodedInput = ">3\r\n$5\r\nhello\r\n$-1\r\n$5\r\nworld\r\n",
            expected = RedisMessage.MessagePushMessage(
                value = listOf(
                    RedisMessage.BulkStringMessage(
                        value = "hello",
                    ),
                    RedisMessage.NullMessage,
                    RedisMessage.BulkStringMessage(
                        value = "world",
                    ),
                ),
            ),
        )
    }

    @Test
    fun unknownMessageType() = runTest {
        val unknownType = '"'
        val typeByte = unknownType.code.toByte()
            .toString()
        val exception = assertFailsWith<RedisMessage.ParsingException> {
            decodeFromString("${unknownType}5\r\n")
        }
        assertContains(
            exception.message
                ?: "",
            typeByte,
        )
    }

    private suspend inline fun <reified T : RedisMessage> testEncodeDecoding(
        expectedEncoded: String,
        expected: T,
    ) {
        val actual = testDecoding(
            encodedInput = expectedEncoded,
            expected = expected,
        )

        testEncoding(
            expectedEncoded = expectedEncoded,
            actual = actual,
        )
    }

    private suspend inline fun <reified T : RedisMessage> testEncoding(
        expectedEncoded: String,
        actual: T,
    ) {
        val actualEncoded = encodeToString(actual)

        assertEquals(expectedEncoded, actualEncoded)
    }

    private suspend inline fun <reified T : RedisMessage> testDecoding(
        encodedInput: String,
        expected: T,
    ): T {
        val actual = decodeFromString(encodedInput)

        assertIs<T>(actual)
        assertEquals(expected, actual)

        return actual
    }

    private suspend fun <T : RedisMessage> encodeToString(
        value: T,
    ): String =
        TestByteWriteChannel()
            .also {
                value.writeTo(it)
            }
            .getAndRestWithoutLocking()
            .decodeToString()

    private suspend fun decodeFromString(
        encodedInput: String,
    ): RedisMessage =
        RedisMessage.parse(
            incoming = ByteReadChannel(encodedInput),
        )
}
