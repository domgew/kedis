package io.github.domgew.kedis

import io.github.domgew.kedis.arguments.SyncOption
import io.github.domgew.kedis.utils.TestConfigUtil
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext

class HashE2ETest {

    @Test
    fun test() = runTest {
        withContext(Dispatchers.Default) {
            val key = "testKey"
            val field1 = "field1"
            val value1 = "value1"
            val field2 = "field2"
            val value2 = "value2"
            val field3 = "field3"
            val value3 = "value3"

            val client = KedisClient.newClient(
                KedisConfiguration(
                    endpoint = KedisConfiguration.Endpoint.HostPort(
                        host = "127.0.0.1",
                        port = TestConfigUtil.getPort(),
                    ),
                    authentication = KedisConfiguration.Authentication.NoAutoAuth,
                    connectionTimeoutMillis = 2_000L,
                ),
            )

            assertTrue(client.flushAll(sync = SyncOption.SYNC))

            assertNull(client.hashGetAll(key))
            assertEquals(0L, client.hashLength(key))
            assertNull(client.hashKeys(key))
            assertFalse(client.hashExists(key, field1))
            assertEquals(0L, client.hashDel(key, field1, field2, field3))

            assertEquals(
                2L,
                client.hashSet(
                    key = key,
                    fieldValues = mapOf(
                        field1 to value1,
                        field2 to value2,
                    ),
                ),
            )
            assertEquals(2L, client.hashLength(key))
            assertTrue(client.hashExists(key, field1))
            assertTrue(client.hashExists(key, field2))
            assertFalse(client.hashExists(key, field3))
            assertEquals(
                listOf(
                    field1,
                    field2,
                ),
                client.hashKeys(key),
            )
            assertEquals(
                mapOf(
                    field1 to value1,
                    field2 to value2,
                ),
                client.hashGetAll(key),
            )
            assertEquals(value2, client.hashGet(key, field2))
            assertEquals(
                1L,
                client.hashSet(
                    key = key,
                    fieldValues = mapOf(
                        field2 to value3,
                        field3 to value3,
                    ),
                ),
            )
            assertEquals(3L, client.hashLength(key))
            assertEquals(value3, client.hashGet(key, field2))
            assertEquals(
                0L,
                client.hashSet(
                    key = key,
                    fieldValues = mapOf(
                        field2 to value2,
                    ),
                ),
            )
            assertTrue(client.hashExists(key, field1))
            assertTrue(client.hashExists(key, field2))
            assertTrue(client.hashExists(key, field3))
            assertEquals(
                listOf(
                    field1,
                    field2,
                    field3,
                ),
                client.hashKeys(key),
            )
            assertEquals(
                mapOf(
                    field1 to value1,
                    field2 to value2,
                    field3 to value3,
                ),
                client.hashGetAll(key),
            )
            assertEquals(
                1L,
                client.hashDel(key, field2),
            )
            assertEquals(2L, client.hashLength(key))
            assertEquals(
                mapOf(
                    field1 to value1,
                    field3 to value3,
                ),
                client.hashGetAll(key),
            )
            assertEquals(
                1L,
                client.del(key),
            )
            assertNull(client.hashGetAll(key))
            assertEquals(0L, client.hashLength(key))

            client.closeSuspended()
        }
    }
}
