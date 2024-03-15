package io.github.domgew.kedis.impl

import io.github.domgew.kedis.utils.CByteWriteChannel
import io.ktor.utils.io.ByteReadChannel
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlinx.coroutines.test.runTest

// see https://redis.io/docs/reference/protocol-spec/
class RedisMessageTest {

    @Test
    fun simpleString() = runTest {
        testEncodeDecoding(
            expectedEncoded = "+OK\r\n",
            expected = RedisMessage.SimpleStringMessage(
                value = "OK",
            ),
        )
    }

    private suspend inline fun <reified T : RedisMessage> testEncodeDecoding(
        expectedEncoded: String,
        expected: T,
    ) {
        val actual = RedisMessage.parse(ByteReadChannel(expectedEncoded))

        assertIs<T>(actual)
        assertEquals(expected, actual)

        val writer = CByteWriteChannel()

        actual.writeTo(writer)

        val actualEncoded = writer.getAndRestWithoutLocking()
            .decodeToString()

        assertEquals(expectedEncoded, actualEncoded)
    }
}
