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
}
