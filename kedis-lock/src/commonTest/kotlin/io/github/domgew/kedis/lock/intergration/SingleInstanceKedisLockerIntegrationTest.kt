package io.github.domgew.kedis.lock.intergration

import io.github.domgew.kedis.KedisClient
import io.github.domgew.kedis.KedisConfiguration
import io.github.domgew.kedis.arguments.server.SyncOption
import io.github.domgew.kedis.commands.KedisServerCommands
import io.github.domgew.kedis.commands.KedisValueCommands
import io.github.domgew.kedis.lock.locker
import io.github.domgew.kedis.lock.utils.TestConfigUtil
import io.github.domgew.kedis.lock.utils.getRedisVersion
import io.github.domgew.kedis.results.value.TtlResult
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.test.asserter
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import net.swiftzer.semver.SemVer

class SingleInstanceKedisLockerIntegrationTest {

    @Test
    fun test() = runTest {
        withContext(Dispatchers.Default) {
            KedisClient.newClient(
                configuration = KedisConfiguration(
                    endpoint = KedisConfiguration.Endpoint.HostPort(
                        host = "127.0.0.1",
                        port = TestConfigUtil.getPort(),
                    ),
                    authentication = KedisConfiguration.Authentication.NoAutoAuth,
                    connectionTimeout = 2.seconds,
                ),
            )
                .use { client ->
                    val redisVersion = client.getRedisVersion()
                        ?: return@use
                    if (redisVersion < SemVer(7)) {
                        println("Needs at least redis 7")
                        return@use
                    }

                    assertTrue(
                        client.execute(
                            command = KedisServerCommands.flushAll(
                                sync = SyncOption.SYNC,
                            ),
                        ),
                    )
                    val lockKey = "lock"
                    val id1 = "id1"
                    val id2 = "id2"
                    val lockDuration = 1.minutes

                    val locker = client.locker()

                    assertTrue(
                        locker.tryLock(
                            key = lockKey,
                            lockerId = id1,
                            maxLockFor = lockDuration,
                        ),
                        "First time locking should succeed",
                    )
                    assertEquals(
                        id1,
                        client.execute(
                            command = KedisValueCommands.get(
                                key = lockKey,
                            ),
                        ),
                        "Lock should be set correctly after first lock",
                    )
                    assertTrue(
                        locker.isLocked(
                            key = lockKey,
                        ),
                        "Lock should be locked after first lock",
                    )
                    assertTrue(
                        locker.isLockedBy(
                            key = lockKey,
                            lockerId = id1,
                        ),
                        "Lock should be locked by first after first lock",
                    )
                    assertFalse(
                        locker.isLockedBy(
                            key = lockKey,
                            lockerId = id2,
                        ),
                        "Lock should not be locked by second after first lock",
                    )
                    client.assertTtl(
                        key = lockKey,
                        range = (lockDuration - 1.seconds)..lockDuration,
                        message = "First lock duration should be correct",
                    )
                    delay(1.seconds)
                    assertTrue(
                        locker.tryLock(
                            key = lockKey,
                            lockerId = id1,
                            maxLockFor = lockDuration,
                        ),
                        "Duplicated locking should succeed",
                    )
                    client.assertTtl(
                        key = lockKey,
                        range = (lockDuration - 2.seconds)..(lockDuration - 1.seconds),
                        message = "First lock duration should stay correct with duplicate lock",
                    )
                    assertEquals(
                        id1,
                        client.execute(
                            command = KedisValueCommands.get(
                                key = lockKey,
                            ),
                        ),
                        "Lock should be set correctly after first lock",
                    )
                    assertFalse(
                        locker.tryLock(
                            key = lockKey,
                            lockerId = id2,
                            maxLockFor = lockDuration,
                        ),
                        "Second lock should fail while first is active",
                    )
                    assertTrue(
                        locker.tryRelease(
                            key = lockKey,
                            lockerId = id2,
                        ),
                        "Second lock should release fine",
                    )
                    assertFalse(
                        locker.tryLock(
                            key = lockKey,
                            lockerId = id2,
                            maxLockFor = lockDuration,
                        ),
                        "Second lock should fail while first is active despite second unlock",
                    )
                    assertTrue(
                        locker.tryRelease(
                            key = lockKey,
                            lockerId = id1,
                        ),
                        "First lock should release fine",
                    )
                    assertNull(
                        client.execute(
                            command = KedisValueCommands.get(
                                key = lockKey,
                            ),
                        ),
                        "Lock should be unlocked after releasing",
                    )
                    assertFalse(
                        locker.isLocked(
                            key = lockKey,
                        ),
                        "Lock should not be locked after releasing",
                    )
                    assertFalse(
                        locker.isLockedBy(
                            key = lockKey,
                            lockerId = id1,
                        ),
                        "Lock should not be locked by first after releasing",
                    )
                    assertFalse(
                        locker.isLockedBy(
                            key = lockKey,
                            lockerId = id2,
                        ),
                        "Lock should not be locked by second after releasing",
                    )
                    assertTrue(
                        locker.tryLock(
                            key = lockKey,
                            lockerId = id2,
                            maxLockFor = lockDuration,
                        ),
                        "Second time locking should succeed after first released",
                    )
                    assertTrue(
                        locker.isLocked(
                            key = lockKey,
                        ),
                        "Lock should be locked after second lock",
                    )
                    assertTrue(
                        locker.isLockedBy(
                            key = lockKey,
                            lockerId = id2,
                        ),
                        "Lock should be locked by second after second lock",
                    )
                    assertFalse(
                        locker.isLockedBy(
                            key = lockKey,
                            lockerId = id1,
                        ),
                        "Lock should not be locked by first after second lock",
                    )
                }
        }
    }

    private suspend fun KedisClient.assertTtl(
        key: String,
        range: ClosedRange<Duration>,
        message: String? = null,
    ) {
        val ttl = when (
            val result = execute(
                command = KedisValueCommands.ttl(
                    key = key,
                ),
            )
        ) {
            is TtlResult.InMilliseconds ->
                result.milliseconds
                    .milliseconds

            is TtlResult.InSeconds ->
                result.seconds
                    .seconds

            TtlResult.Never ->
                Duration.INFINITE

            TtlResult.NotFound ->
                asserter.fail("Key '$key' not found")
        }

        assertContains(range, ttl, message)
    }
}
