package io.github.domgew.kedis.utils

import io.github.domgew.kedis.impl.RedisMessage
import kotlin.test.assertEquals
import kotlin.test.assertIs

object RedisUtil {
    internal suspend fun createUser(
        username: String,
        password: String,
    ) {
        SocketUtil.withConnectedSocket {
            RedisMessage.ArrayMessage(
                value = listOf(
                    RedisMessage.BulkStringMessage("ACL"),
                    RedisMessage.BulkStringMessage("SETUSER"),
                    RedisMessage.BulkStringMessage(username),
                    RedisMessage.BulkStringMessage("ON"),
                    RedisMessage.BulkStringMessage(">$password"),
                    RedisMessage.BulkStringMessage("+@all"),
                    RedisMessage.BulkStringMessage("~*"),
                ),
            )
                .writeTo(writeChannel)
            writeChannel.flush()

            val createResult = RedisMessage.parse(readChannel)

            if (createResult is RedisMessage.ErrorMessage) {
                throw Exception(createResult.value)
            }

            assertIs<RedisMessage.StringMessage>(createResult)
            assertEquals("OK", createResult.value)
        }
    }

    internal suspend fun removeUser(
        username: String,
    ) {
        SocketUtil.withConnectedSocket {
            RedisMessage.ArrayMessage(
                value = listOf(
                    RedisMessage.BulkStringMessage("ACL"),
                    RedisMessage.BulkStringMessage("DELUSER"),
                    RedisMessage.BulkStringMessage(username),
                ),
            )
                .writeTo(writeChannel)
            writeChannel.flush()

            val deleteResult = RedisMessage.parse(readChannel)

            assertIs<RedisMessage.IntegerMessage>(deleteResult)
            assertEquals(1, deleteResult.value)
        }
    }
}
