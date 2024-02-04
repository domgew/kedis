package io.github.domgew.kedis

import io.github.domgew.kedis.arguments.InfoSectionName
import io.github.domgew.kedis.arguments.SetOptions
import io.github.domgew.kedis.arguments.SyncOption
import io.github.domgew.kedis.results.server.InfoSection
import io.github.domgew.kedis.results.value.SetBinaryResult
import io.github.domgew.kedis.results.value.SetResult
import io.github.domgew.kedis.utils.TestConfigUtil
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext

class SimpleE2ETest {
    @Test
    fun ping() = runTest {
        withContext(Dispatchers.Default) {
            val pingContent = "_TEST_"

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
            val pongMessage = client.ping(pingContent)
            client.closeSuspended()

            assertEquals(pingContent, pongMessage)
        }
    }

    @Test
    fun flushGetExistsSetGetExistsDelGetExistsBin() = runTest {
        withContext(Dispatchers.Default) {
            val testKey1 = "test1"
            val testValue = "testValue1"
            val testKey2 = "test2"
            val testValueBin = Random.nextBytes(384_738)

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
            assertNull(client.get(testKey1))
            assertNull(client.get(testKey2))
            assertEquals(0L, client.exists(testKey1, testKey2))
            assertEquals(SetResult.Ok, client.set(testKey1, testValue))
            assertEquals(testValue, client.get(testKey1))
            assertNull(client.get(testKey2))
            assertEquals(1L, client.exists(testKey1, testKey2))
            assertEquals(1L, client.del(testKey1, testKey2))
            assertNull(client.get(testKey1))
            assertNull(client.get(testKey2))
            assertEquals(0L, client.exists(testKey1, testKey2))
            assertNull(client.getBinary(testKey1))
            assertNull(client.getBinary(testKey2))
            assertEquals(SetBinaryResult.Ok, client.setBinary(testKey1, testValueBin))
            assertContentEquals(testValueBin, client.getBinary(testKey1))
            assertNull(client.getBinary(testKey2))
            assertEquals(1L, client.exists(testKey1, testKey2))
            assertEquals(1L, client.del(testKey1))
            assertEquals(0L, client.exists(testKey1, testKey2))
            assertEquals(
                SetResult.Ok,
                client.set(
                    key = testKey1,
                    value = testValue,
                    options = SetOptions(
                        previousKeyHandling = SetOptions.PreviousKeyHandling.KEEP_IF_EXISTS,
                        expire = SetOptions.ExpireOption.ExpiresInMilliseconds(
                            milliseconds = 60_000L,
                        ),
                    ),
                ),
            )
            assertEquals(
                SetResult.Aborted,
                client.set(
                    key = testKey1,
                    value = testValue,
                    options = SetOptions(
                        previousKeyHandling = SetOptions.PreviousKeyHandling.KEEP_IF_EXISTS,
                        expire = SetOptions.ExpireOption.ExpiresInMilliseconds(
                            milliseconds = 60_000L,
                        ),
                    ),
                ),
            )
            assertEquals(
                SetResult.PreviousValue(
                    value = testValue,
                ),
                client.set(
                    key = testKey1,
                    value = testValue,
                    options = SetOptions(
                        expire = SetOptions.ExpireOption.ExpiresInMilliseconds(
                            milliseconds = 60_000L,
                        ),
                        getPreviousValue = true,
                    ),
                ),
            )
            assertEquals(1L, client.del(testKey1))
            assertEquals(
                SetResult.NotFound,
                client.set(
                    key = testKey1,
                    value = testValue,
                    options = SetOptions(
                        previousKeyHandling = SetOptions.PreviousKeyHandling.OVERRIDE,
                        expire = SetOptions.ExpireOption.ExpiresInMilliseconds(
                            milliseconds = 60_000L,
                        ),
                        getPreviousValue = true,
                    ),
                ),
            )

            client.closeSuspended()
        }
    }

    @Test
    fun infoServer() = runTest {
        withContext(Dispatchers.Default) {
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

            val infoList = client.info(InfoSectionName.SERVER)
            client.closeSuspended()

            assertEquals(1, infoList.size)

            val serverInfo = infoList.first()

            assertIs<InfoSection.Server>(serverInfo)
            assertNotNull(serverInfo.os)
            assertNotNull(serverInfo.processId)
            assertNotNull(serverInfo.redisVersion)
        }
    }

    @Test
    fun incrementalChanges() = runTest {
        withContext(Dispatchers.Default) {
            val floatKey = "testKeyFloat"
            val intKey = "testKeyInt"
            val strKey = "testKeyStr"

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
            assertNull(client.get(intKey))
            assertEquals(1, client.incr(intKey))
            assertEquals(0, client.decr(intKey))
            assertEquals(-1, client.decr(intKey))
            assertEquals(0, client.incr(intKey))
            assertEquals(5, client.incrBy(intKey, 5))
            assertEquals(7, client.incrBy(intKey, 2))
            assertEquals(1, client.decrBy(intKey, 6))
            assertNull(client.get(floatKey))
            assertEquals(2.65, client.incrByFloat(floatKey, 2.65))
            assertEquals(2.63, client.incrByFloat(floatKey, -0.02))
            assertEquals(3.15, client.incrByFloat(floatKey, 0.52))
            assertEquals(1.52, client.incrByFloat(intKey, 0.52))
            assertNull(client.get(strKey))
            assertEquals(0, client.strLen(strKey))
            assertEquals("", client.getRange(strKey, 0, 5))
            assertEquals("", client.getRange(strKey, 0L..5L))
            assertEquals(4, client.append(strKey, "test"))
            assertEquals(4, client.strLen(strKey))
            assertEquals("test", client.get(strKey))
            assertEquals("test", client.getRange(strKey, 0, 5))
            assertEquals("test", client.getRange(strKey, 0L..5L))
            assertEquals(8, client.append(strKey, "Test"))
            assertEquals(8, client.strLen(strKey))
            assertEquals("testTest", client.get(strKey))
            assertEquals("testTe", client.getRange(strKey, 0, 5))
            assertEquals("testTest".slice(0..5), client.getRange(strKey, 0L..5L))
            assertEquals("testTest".slice(0 until 5), client.getRange(strKey, 0L until 5L))
            assertFailsWith<KedisException.RedisErrorResponseException> {
                client.incrByFloat(strKey, 0.5)
            }
        }
    }
}
