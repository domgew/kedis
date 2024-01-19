package io.github.domgew.kedis

import io.github.domgew.kedis.impl.RedisMessage
import io.github.domgew.kedis.utils.SocketUtil
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext

class RedisMessageIntegrationTest {
    @Test
    fun basicPing() = runTest {
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

    @Test
    fun unicodePing() = runTest {
        withContext(Dispatchers.Default) {
            val pingContent = "äöüßéèÖÄÜ€áàúùóòŕâôûîìí"

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

    @Test
    fun longStringPing() = runTest {
        withContext(Dispatchers.Default) {
            val pingContent = getRandomString(5_368_342)

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

    @Test
    fun longBytePing() = runTest {
        withContext(Dispatchers.Default) {
            val pingContent = Random.nextBytes(5_368_342)

            val pongMessage = SocketUtil.withConnectedSocket {
                val pingMessage = RedisMessage.ArrayMessage(
                    value = listOf(
                        RedisMessage.BulkStringMessage(
                            value = "PING",
                        ),
                        RedisMessage.BulkStringMessage(
                            data = pingContent,
                        ),
                    ),
                )

                pingMessage.writeTo(writeChannel)
                writeChannel.flush()

                return@withConnectedSocket RedisMessage.parse(readChannel)
            }

            assertIs<RedisMessage.BulkStringMessage>(pongMessage)
            assertContentEquals(pingContent, pongMessage.data)
        }
    }

    private fun getRandomString(length: Int) : String {
        val allowedChars = ('A'..'Z') + ('a'..'z') + ('0'..'9')
        return (1..length)
            .map { allowedChars.random() }
            .joinToString("")
    }
}
