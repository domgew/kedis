package io.github.domgew.kedis

import io.github.domgew.kedis.impl.RedisMessage
import io.github.domgew.kedis.utils.SocketUtil
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext

class RedisMessageIntegrationTest {
    @Test
    fun testPing() = runTest {
        withContext(Dispatchers.Default) {
            val pingContent = "_TEST_"

            val pongMessage = SocketUtil.withConnectedSocket {
                val pingMessage = RedisMessage.ArrayMessage(
                    value = listOf(
                        RedisMessage.BulkStringMessage(
                            value = "PING",
                        ),
                        RedisMessage.BulkStringMessage(
                            value = pingContent,
                        ),
                    ),
                )

                pingMessage.writeTo(writeChannel)
                writeChannel.flush()

                return@withConnectedSocket RedisMessage.parse(readChannel)
            }

            assertIs<RedisMessage.BulkStringMessage>(pongMessage)
            assertEquals(pingContent, pongMessage.value)
        }
    }
}
