package io.github.domgew.kedis

import io.github.domgew.kedis.arguments.server.InfoSectionName
import io.github.domgew.kedis.arguments.server.SyncOption
import io.github.domgew.kedis.arguments.value.SetOptions
import io.github.domgew.kedis.commands.KedisServerCommands
import io.github.domgew.kedis.commands.KedisValueCommands
import io.github.domgew.kedis.results.server.InfoSection
import io.github.domgew.kedis.results.value.ExpireTimeResult
import io.github.domgew.kedis.results.value.SetBinaryResult
import io.github.domgew.kedis.results.value.SetResult
import io.github.domgew.kedis.results.value.TtlResult
import io.github.domgew.kedis.utils.TestConfigUtil
import io.github.domgew.kedis.utils.getRedisVersion
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Clock
import kotlin.time.Duration.Companion.seconds
import kotlin.time.ExperimentalTime
import kotlin.time.TimeSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import net.swiftzer.semver.SemVer

@OptIn(ExperimentalTime::class)
class SimpleE2ETest {

    @Test
    fun ping() = runTest {
        withContext(Dispatchers.Default) {
            val pingContent = "_TEST_"

            val pongMessage = KedisClient.newClient(
                KedisConfiguration(
                    endpoint = KedisConfiguration.Endpoint.HostPort(
                        host = "127.0.0.1",
                        port = TestConfigUtil.getPort(),
                    ),
                    authentication = KedisConfiguration.Authentication.NoAutoAuth,
                    connectionTimeout = 2.seconds,
                ),
            )
                .use { client ->
                    client.execute(
                        command = KedisServerCommands.ping(
                            content = pingContent,
                        ),
                    )
                }

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

            KedisClient.newClient(
                KedisConfiguration(
                    endpoint = KedisConfiguration.Endpoint.HostPort(
                        host = "127.0.0.1",
                        port = TestConfigUtil.getPort(),
                    ),
                    authentication = KedisConfiguration.Authentication.NoAutoAuth,
                    connectionTimeout = 2.seconds,
                ),
            )
                .use { client ->
                    assertTrue(
                        client.execute(
                            command = KedisServerCommands.flushAll(
                                sync = SyncOption.SYNC,
                            ),
                        ),
                    )
                    assertNull(
                        client.execute(
                            command = KedisValueCommands.get(
                                key = testKey1,
                            ),
                        ),
                    )
                    assertNull(
                        client.execute(
                            command = KedisValueCommands.get(
                                key = testKey2,
                            ),
                        ),
                    )
                    assertEquals(
                        0L,
                        client.execute(
                            command = KedisValueCommands.exists(
                                key = arrayOf(testKey1, testKey2),
                            ),
                        ),
                    )
                    assertEquals(
                        SetResult.Ok,
                        client.execute(
                            command = KedisValueCommands.set(
                                key = testKey1,
                                value = testValue,
                            ),
                        ),
                    )
                    assertEquals(
                        testValue,
                        client.execute(
                            command = KedisValueCommands.get(
                                key = testKey1,
                            ),
                        ),
                    )
                    assertNull(
                        client.execute(
                            command = KedisValueCommands.get(
                                key = testKey2,
                            ),
                        ),
                    )
                    assertEquals(
                        1L,
                        client.execute(
                            command = KedisValueCommands.exists(
                                key = arrayOf(testKey1, testKey2),
                            ),
                        ),
                    )
                    assertEquals(
                        1L,
                        client.execute(
                            command = KedisValueCommands.del(
                                key = arrayOf(testKey1, testKey2),
                            ),
                        ),
                    )
                    assertNull(
                        client.execute(
                            command = KedisValueCommands.get(
                                key = testKey1,
                            ),
                        ),
                    )
                    assertNull(
                        client.execute(
                            command = KedisValueCommands.get(
                                key = testKey2,
                            ),
                        ),
                    )
                    assertEquals(
                        0L,
                        client.execute(
                            command = KedisValueCommands.exists(
                                key = arrayOf(testKey1, testKey2),
                            ),
                        ),
                    )
                    assertNull(
                        client.execute(
                            command = KedisValueCommands.getBinary(
                                key = testKey1,
                            ),
                        ),
                    )
                    assertNull(
                        client.execute(
                            command = KedisValueCommands.getBinary(
                                key = testKey2,
                            ),
                        ),
                    )
                    assertEquals(
                        SetBinaryResult.Ok,
                        client.execute(
                            command = KedisValueCommands.setBinary(
                                key = testKey1,
                                value = testValueBin,
                            ),
                        ),
                    )
                    assertContentEquals(
                        testValueBin,
                        client.execute(
                            command = KedisValueCommands.getBinary(
                                key = testKey1,
                            ),
                        ),
                    )
                    assertNull(
                        client.execute(
                            command = KedisValueCommands.getBinary(
                                key = testKey2,
                            ),
                        ),
                    )
                    assertEquals(
                        1L,
                        client.execute(
                            command = KedisValueCommands.exists(
                                key = arrayOf(testKey1, testKey2),
                            ),
                        ),
                    )
                    assertEquals(
                        1L,
                        client.execute(
                            command = KedisValueCommands.del(
                                key = arrayOf(testKey1),
                            ),
                        ),
                    )
                    assertEquals(
                        0L,
                        client.execute(
                            command = KedisValueCommands.exists(
                                key = arrayOf(testKey1, testKey2),
                            ),
                        ),
                    )
                    assertEquals(
                        SetResult.Ok,
                        client.execute(
                            command = KedisValueCommands.set(
                                key = testKey1,
                                value = testValue,
                                options = SetOptions(
                                    previousKeyHandling = SetOptions.PreviousKeyHandling.KEEP_IF_EXISTS,
                                    expire = SetOptions.ExpireOption.ExpiresInMilliseconds(
                                        milliseconds = 60_000L,
                                    ),
                                ),
                            ),
                        ),
                    )
                    assertEquals(
                        SetResult.Aborted,
                        client.execute(
                            command = KedisValueCommands.set(
                                key = testKey1,
                                value = testValue,
                                options = SetOptions(
                                    previousKeyHandling = SetOptions.PreviousKeyHandling.KEEP_IF_EXISTS,
                                    expire = SetOptions.ExpireOption.ExpiresInMilliseconds(
                                        milliseconds = 60_000L,
                                    ),
                                ),
                            ),
                        ),
                    )
                    assertEquals(
                        SetResult.PreviousValue(
                            value = testValue,
                        ),
                        client.execute(
                            command = KedisValueCommands.set(
                                key = testKey1,
                                value = testValue,
                                options = SetOptions(
                                    expire = SetOptions.ExpireOption.ExpiresInMilliseconds(
                                        milliseconds = 60_000L,
                                    ),
                                    getPreviousValue = true,
                                ),
                            ),
                        ),
                    )
                    assertEquals(
                        1L,
                        client.execute(
                            command = KedisValueCommands.del(
                                key = arrayOf(testKey1),
                            ),
                        ),
                    )
                    assertEquals(
                        SetResult.NotFound,
                        client.execute(
                            command = KedisValueCommands.set(
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
                        ),
                    )
                }
        }
    }

    @Test
    fun expireTimeTests() = runTest {
        withContext(Dispatchers.Default) {
            val testKey1 = "test1"
            val testValue = "testValue1"

            KedisClient.newClient(
                KedisConfiguration(
                    endpoint = KedisConfiguration.Endpoint.HostPort(
                        host = "127.0.0.1",
                        port = TestConfigUtil.getPort(),
                    ),
                    authentication = KedisConfiguration.Authentication.NoAutoAuth,
                    connectionTimeout = 2.seconds,
                ),
            )
                .use { client ->

                    assertTrue(
                        client.execute(
                            command = KedisServerCommands.flushAll(
                                sync = SyncOption.SYNC,
                            ),
                        ),
                    )

                    val redisVersion = client.getRedisVersion()
                        ?: return@withContext

                    if (redisVersion < SemVer.parse("7.0.0")) {
                        return@withContext
                    }

                    assertNull(
                        client.execute(
                            command = KedisValueCommands.get(
                                key = testKey1,
                            ),
                        ),
                    )
                    assertEquals(
                        ExpireTimeResult.NotFound,
                        client.execute(
                            command = KedisValueCommands.expireTime(
                                key = testKey1,
                            ),
                        ),
                    )

                    client.execute(
                        command = KedisValueCommands.set(
                            key = testKey1,
                            value = testValue,
                        ),
                    )
                    assertEquals(
                        ExpireTimeResult.Never,
                        client.execute(
                            command = KedisValueCommands.expireTime(
                                key = testKey1,
                            ),
                        ),
                    )

                    // SECONDS
                    val atSeconds = Clock.System
                        .now()
                        .epochSeconds + 31

                    client.execute(
                        command = KedisValueCommands.set(
                            key = testKey1,
                            value = testValue,
                            options = SetOptions(
                                expire = SetOptions.ExpireOption.ExpiresAtUnixEpochSecond(
                                    unixEpochSecond = atSeconds,
                                ),
                            ),
                        ),
                    )
                    var result: ExpireTimeResult = client.execute(
                        command = KedisValueCommands.expireTime(
                            key = testKey1,
                            inMilliseconds = false,
                        ),
                    )

                    assertIs<ExpireTimeResult.AtUnixSecond>(result)
                    assertEquals(atSeconds, result.seconds)

                    // MILLISECONDS

                    val atMilliseconds = Clock.System.now()
                        .toEpochMilliseconds() + 31_000

                    client.execute(
                        command = KedisValueCommands.set(
                            key = testKey1,
                            value = testValue,
                            options = SetOptions(
                                expire = SetOptions.ExpireOption.ExpiresAtUnixEpochMillisecond(
                                    unixEpochMillisecond = atMilliseconds,
                                ),
                            ),
                        ),
                    )
                    result = client.execute(
                        command = KedisValueCommands.expireTime(
                            key = testKey1,
                            inMilliseconds = true,
                        ),
                    )

                    assertIs<ExpireTimeResult.AtUnixMillisecond>(result)
                    assertEquals(atMilliseconds, result.milliseconds)
                }
        }
    }

    @Test
    fun setWithExpiryTests() = runTest {
        withContext(Dispatchers.Default) {
            val testKey1 = "test1"
            val testValue = "testValue1"
            val timeSource = TimeSource.Monotonic

            KedisClient.newClient(
                KedisConfiguration(
                    endpoint = KedisConfiguration.Endpoint.HostPort(
                        host = "127.0.0.1",
                        port = TestConfigUtil.getPort(),
                    ),
                    authentication = KedisConfiguration.Authentication.NoAutoAuth,
                    connectionTimeout = 2.seconds,
                ),
            )
                .use { client ->
                    @Suppress("JoinDeclarationAndAssignment")
                    var markBefore: TimeSource.Monotonic.ValueTimeMark

                    @Suppress("JoinDeclarationAndAssignment")
                    var markAfter: TimeSource.Monotonic.ValueTimeMark

                    @Suppress("JoinDeclarationAndAssignment")
                    var timeTaken: Long

                    assertTrue(
                        client.execute(
                            command = KedisServerCommands.flushAll(
                                sync = SyncOption.SYNC,
                            ),
                        ),
                    )
                    assertNull(
                        client.execute(
                            command = KedisValueCommands.get(
                                key = testKey1,
                            ),
                        ),
                    )

                    assertEquals(
                        TtlResult.NotFound,
                        client.execute(
                            command = KedisValueCommands.ttl(
                                key = testKey1,
                            ),
                        ),
                    )
                    client.execute(
                        command = KedisValueCommands.set(
                            key = testKey1,
                            value = testValue,
                        ),
                    )
                    assertEquals(
                        TtlResult.Never,
                        client.execute(
                            command = KedisValueCommands.ttl(
                                key = testKey1,
                            ),
                        ),
                    )

                    // IN SECONDS

                    val ttlSeconds = 30L
                    markBefore = timeSource.markNow()
                    client.execute(
                        command = KedisValueCommands.set(
                            key = testKey1,
                            value = testValue,
                            options = SetOptions(
                                expire = SetOptions.ExpireOption.ExpiresInSeconds(
                                    seconds = ttlSeconds,
                                ),
                            ),
                        ),
                    )
                    var ttlResult: TtlResult = client.execute(
                        command = KedisValueCommands.ttl(
                            key = testKey1,
                            inMilliseconds = false,
                        ),
                    )
                    markAfter = timeSource.markNow()
                    timeTaken = (markAfter - markBefore).inWholeMilliseconds + 1

                    assertIs<TtlResult.InSeconds>(ttlResult)
                    assertContains(
                        range = (ttlSeconds - timeTaken / 1000 - 1)..ttlSeconds,
                        value = ttlResult.seconds,
                    )

                    // IN MILLISECONDS

                    val ttlMilliseconds = 30_000L
                    markBefore = timeSource.markNow()
                    client.execute(
                        command = KedisValueCommands.set(
                            key = testKey1,
                            value = testValue,
                            options = SetOptions(
                                expire = SetOptions.ExpireOption.ExpiresInMilliseconds(
                                    milliseconds = ttlMilliseconds,
                                ),
                            ),
                        ),
                    )
                    ttlResult = client.execute(
                        command = KedisValueCommands.ttl(
                            key = testKey1,
                            inMilliseconds = true,
                        ),
                    )
                    markAfter = timeSource.markNow()
                    timeTaken = (markAfter - markBefore).inWholeMilliseconds + 1

                    assertIs<TtlResult.InMilliseconds>(ttlResult)
                    assertContains(
                        range = (ttlMilliseconds - timeTaken)..ttlMilliseconds,
                        value = ttlResult.milliseconds,
                    )

                    // AT SECONDS

                    val atSecond = Clock.System.now().epochSeconds + ttlSeconds
                    markBefore = timeSource.markNow()
                    client.execute(
                        command = KedisValueCommands.set(
                            key = testKey1,
                            value = testValue,
                            options = SetOptions(
                                expire = SetOptions.ExpireOption.ExpiresAtUnixEpochSecond(
                                    unixEpochSecond = atSecond,
                                ),
                            ),
                        ),
                    )
                    ttlResult = client.execute(
                        command = KedisValueCommands.ttl(
                            key = testKey1,
                            inMilliseconds = false,
                        ),
                    )
                    markAfter = timeSource.markNow()
                    timeTaken = (markAfter - markBefore).inWholeMilliseconds + 1

                    assertIs<TtlResult.InSeconds>(ttlResult)
                    assertContains(
                        range = (ttlSeconds - timeTaken / 1000 - 1)..ttlSeconds,
                        value = ttlResult.seconds,
                    )

                    // AT MILLISECONDS

                    val atMillisecond = Clock.System.now()
                        .toEpochMilliseconds() + ttlMilliseconds
                    markBefore = timeSource.markNow()
                    client.execute(
                        command = KedisValueCommands.set(
                            key = testKey1,
                            value = testValue,
                            options = SetOptions(
                                expire = SetOptions.ExpireOption.ExpiresAtUnixEpochMillisecond(
                                    unixEpochMillisecond = atMillisecond,
                                ),
                            ),
                        ),
                    )
                    ttlResult = client.execute(
                        command = KedisValueCommands.ttl(
                            key = testKey1,
                            inMilliseconds = true,
                        ),
                    )
                    markAfter = timeSource.markNow()
                    timeTaken = (markAfter - markBefore).inWholeMilliseconds + 1

                    assertIs<TtlResult.InMilliseconds>(ttlResult)
                    assertContains(
                        range = (ttlMilliseconds - timeTaken)..ttlMilliseconds,
                        value = ttlResult.milliseconds,
                    )

                    val delay = 1_000L
                    delay(delay)

                    client.execute(
                        command = KedisValueCommands.set(
                            key = testKey1,
                            value = testValue,
                            options = SetOptions(
                                expire = SetOptions.ExpireOption.KeepPreviousTTL,
                            ),
                        ),
                    )
                    ttlResult = client.execute(
                        command = KedisValueCommands.ttl(
                            key = testKey1,
                            inMilliseconds = true,
                        ),
                    )
                    markAfter = timeSource.markNow()
                    timeTaken = (markAfter - markBefore).inWholeMilliseconds + 1

                    assertIs<TtlResult.InMilliseconds>(ttlResult)
                    assertContains(
                        range = (ttlMilliseconds - timeTaken)..(ttlMilliseconds - delay),
                        value = ttlResult.milliseconds,
                    )
                }
        }
    }

    @Test
    fun infoServer() = runTest {
        withContext(Dispatchers.Default) {
            val infoList = KedisClient.newClient(
                KedisConfiguration(
                    endpoint = KedisConfiguration.Endpoint.HostPort(
                        host = "127.0.0.1",
                        port = TestConfigUtil.getPort(),
                    ),
                    authentication = KedisConfiguration.Authentication.NoAutoAuth,
                    connectionTimeout = 2.seconds,
                ),
            )
                .use { client ->
                    client.execute(
                        command = KedisServerCommands.info(
                            section = arrayOf(InfoSectionName.SERVER),
                        ),
                    )
                }

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

            KedisClient.newClient(
                KedisConfiguration(
                    endpoint = KedisConfiguration.Endpoint.HostPort(
                        host = "127.0.0.1",
                        port = TestConfigUtil.getPort(),
                    ),
                    authentication = KedisConfiguration.Authentication.NoAutoAuth,
                    connectionTimeout = 2.seconds,
                ),
            )
                .use { client ->
                    assertTrue(
                        client.execute(
                            command = KedisServerCommands.flushAll(
                                sync = SyncOption.SYNC,
                            ),
                        ),
                    )
                    assertNull(
                        client.execute(
                            command = KedisValueCommands.get(
                                key = intKey,
                            ),
                        ),
                    )
                    assertEquals(
                        1,
                        client.execute(
                            command = KedisValueCommands.incr(
                                key = intKey,
                            ),
                        ),
                    )
                    assertEquals(
                        0,
                        client.execute(
                            command = KedisValueCommands.decr(
                                key = intKey,
                            ),
                        ),
                    )
                    assertEquals(
                        -1,
                        client.execute(
                            command = KedisValueCommands.decr(
                                key = intKey,
                            ),
                        ),
                    )
                    assertEquals(
                        0,
                        client.execute(
                            command = KedisValueCommands.incr(
                                key = intKey,
                            ),
                        ),
                    )
                    assertEquals(
                        5,
                        client.execute(
                            command = KedisValueCommands.incrBy(
                                key = intKey,
                                by = 5,
                            ),
                        ),
                    )
                    assertEquals(
                        7,
                        client.execute(
                            command = KedisValueCommands.incrBy(
                                key = intKey,
                                by = 2,
                            ),
                        ),
                    )
                    assertEquals(
                        1,
                        client.execute(
                            command = KedisValueCommands.decrBy(
                                key = intKey,
                                by = 6,
                            ),
                        ),
                    )
                    assertNull(
                        client.execute(
                            command = KedisValueCommands.get(
                                key = floatKey,
                            ),
                        ),
                    )
                    assertEquals(
                        2.65,
                        client.execute(
                            command = KedisValueCommands.incrByFloat(
                                key = floatKey,
                                by = 2.65,
                            ),
                        ),
                    )
                    assertEquals(
                        2.63,
                        client.execute(
                            command = KedisValueCommands.incrByFloat(
                                key = floatKey,
                                by = -0.02,
                            ),
                        ),
                    )
                    assertEquals(
                        3.15,
                        client.execute(
                            command = KedisValueCommands.incrByFloat(
                                key = floatKey,
                                by = 0.52,
                            ),
                        ),
                    )
                    assertEquals(
                        1.52,
                        client.execute(
                            command = KedisValueCommands.incrByFloat(
                                key = intKey,
                                by = 0.52,
                            ),
                        ),
                    )
                    assertNull(
                        client.execute(
                            command = KedisValueCommands.get(
                                key = strKey,
                            ),
                        ),
                    )
                    assertEquals(
                        0,
                        client.execute(
                            command = KedisValueCommands.strLen(
                                key = strKey,
                            ),
                        ),
                    )
                    assertEquals(
                        "",
                        client.execute(
                            command = KedisValueCommands.getRange(
                                key = strKey,
                                start = 0,
                                end = 5,
                            ),
                        ),
                    )
                    assertEquals(
                        "",
                        client.execute(
                            command = KedisValueCommands.getRange(
                                key = strKey,
                                range = 0L..5L,
                            ),
                        ),
                    )
                    assertEquals(
                        4,
                        client.execute(
                            command = KedisValueCommands.append(
                                key = strKey,
                                value = "test",
                            ),
                        ),
                    )
                    assertEquals(
                        4,
                        client.execute(
                            command = KedisValueCommands.strLen(
                                key = strKey,
                            ),
                        ),
                    )
                    assertEquals(
                        "test",
                        client.execute(
                            command = KedisValueCommands.get(
                                key = strKey,
                            ),
                        ),
                    )
                    assertEquals(
                        "test",
                        client.execute(
                            command = KedisValueCommands.getRange(
                                key = strKey,
                                start = 0,
                                end = 5,
                            ),
                        ),
                    )
                    assertEquals(
                        "test",
                        client.execute(
                            command = KedisValueCommands.getRange(
                                key = strKey,
                                range = 0L..5L,
                            ),
                        ),
                    )
                    assertEquals(
                        8,
                        client.execute(
                            command = KedisValueCommands.append(
                                key = strKey,
                                value = "Test",
                            ),
                        ),
                    )
                    assertEquals(
                        8,
                        client.execute(
                            command = KedisValueCommands.strLen(
                                key = strKey,
                            ),
                        ),
                    )
                    assertEquals(
                        "testTest",
                        client.execute(
                            command = KedisValueCommands.get(
                                key = strKey,
                            ),
                        ),
                    )
                    assertEquals(
                        "testTe",
                        client.execute(
                            command = KedisValueCommands.getRange(
                                key = strKey,
                                start = 0,
                                end = 5,
                            ),
                        ),
                    )
                    assertEquals(
                        "testTest".slice(0..5),
                        client.execute(
                            command = KedisValueCommands.getRange(
                                key = strKey,
                                range = 0L..5L,
                            ),
                        ),
                    )
                    assertEquals(
                        "testTest".slice(0 until 5),
                        client.execute(
                            command = KedisValueCommands.getRange(
                                key = strKey,
                                range = 0L until 5L,
                            ),
                        ),
                    )
                    assertFailsWith<KedisException.RedisErrorResponseException> {
                        client.execute(
                            command = KedisValueCommands.incrByFloat(
                                key = strKey,
                                by = 0.5,
                            ),
                        )
                    }
                }
        }
    }

    @Test
    fun bgSave() = runTest {
        withContext(Dispatchers.Default) {
            KedisClient.newClient(
                KedisConfiguration(
                    endpoint = KedisConfiguration.Endpoint.HostPort(
                        host = "127.0.0.1",
                        port = TestConfigUtil.getPort(),
                    ),
                    authentication = KedisConfiguration.Authentication.NoAutoAuth,
                    connectionTimeout = 2.seconds,
                ),
            )
                .use { client ->
                    assertTrue(
                        client.execute(
                            command = KedisServerCommands.flushAll(
                                sync = SyncOption.SYNC,
                            ),
                        ),
                    )

                    client.execute(
                        command = KedisServerCommands.bgSave(
                            schedule = false,
                        ),
                    )
                    delay(1_000)
                    client.execute(
                        command = KedisServerCommands.bgSave(
                            schedule = true,
                        ),
                    )
                }
        }
    }
}
