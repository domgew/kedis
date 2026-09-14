package io.github.domgew.kedis

import com.sun.management.UnixOperatingSystemMXBean
import io.github.domgew.kedis.commands.KedisServerCommands
import io.github.domgew.kedis.utils.TestConfigUtil
import io.ktor.network.selector.SelectorManager
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.fail
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.lang.management.ManagementFactory

/**
 * Regression test for never-closing selector managers per client leaking epoll file descriptors on Linux and macOS.
 * 
 * AI-assisted
 */
class ManagedSelectorFileDescriptorRegressionTest {

    private companion object {

        const val PING_CONTENT = "_TEST_"

        const val WARM_UP_ITERATIONS = 5
        const val MEASURED_ITERATIONS = 50

        // socket and selector teardown are asynchronous, so a few descriptors may still be in flight
        const val ALLOWED_DELTA = 10L

        val SETTLE_POLL_INTERVAL = 100.milliseconds
        val SETTLE_TIMEOUT = 20.seconds
        val CONNECT_TIMEOUT = 2.seconds
        val TEST_TIMEOUT = 2.minutes

        val OS_NAME: String = System.getProperty("os.name")
            ?: "unknown"
    }

    @Test
    fun managedSelectorManager_doesNotLeakFileDescriptors() = runTest(
        timeout = TEST_TIMEOUT,
    ) {
        val osBean = unixOsBeanOrNull()
            ?: return@runTest

        withContext(Dispatchers.Default) {
            // class loading, jit and the redis connection warm up open descriptors of their own
            repeat(WARM_UP_ITERATIONS) {
                pingOnce()
            }

            val before = awaitStableFileDescriptorCount(
                osBean = osBean,
            )

            repeat(MEASURED_ITERATIONS) {
                pingOnce()
            }

            val after = awaitFileDescriptorCount(
                osBean = osBean,
                threshold = before + ALLOWED_DELTA,
            )

            assertTrue(
                after <= before + ALLOWED_DELTA,
                "leaked ${after - before} file descriptors over $MEASURED_ITERATIONS client life cycles"
                    .plus(" (before=$before, after=$after)"),
            )
        }
    }

    @Test
    fun injectedSelectorManager_doesNotLeakFileDescriptors() = runTest(
        timeout = TEST_TIMEOUT,
    ) {
        val osBean = unixOsBeanOrNull()
            ?: return@runTest

        withContext(Dispatchers.Default) {
            repeat(WARM_UP_ITERATIONS) {
                pingOnce()
            }

            val before = awaitStableFileDescriptorCount(
                osBean = osBean,
            )

            SelectorManager(Dispatchers.IO)
                .use { selectorManager ->
                    repeat(MEASURED_ITERATIONS) {
                        pingOnce(
                            selectorManager = selectorManager,
                        )
                    }
                }

            val after = awaitFileDescriptorCount(
                osBean = osBean,
                threshold = before + ALLOWED_DELTA,
            )

            assertTrue(
                after <= before + ALLOWED_DELTA,
                "leaked ${after - before} file descriptors over $MEASURED_ITERATIONS client life cycles"
                    .plus(" (before=$before, after=$after)"),
            )
        }
    }

    // null on Windows only
    private fun unixOsBeanOrNull(): UnixOperatingSystemMXBean? {
        if (!isFileDescriptorAwarePlatform()) {
            return null
        }

        val osBean = ManagementFactory.getOperatingSystemMXBean()

        if (osBean !is UnixOperatingSystemMXBean) {
            fail(
                "expected a ${UnixOperatingSystemMXBean::class.qualifiedName} on $OS_NAME, got ${osBean::class.qualifiedName}",
            )
        }

        // unimplemented backends report -1 instead of throwing
        val openFileDescriptorCount = osBean.openFileDescriptorCount
            .takeIf {
                it > 0
            }

        if (openFileDescriptorCount == null) {
            fail(
                "expected a positive open file descriptor count on $OS_NAME",
            )
        }

        return osBean
    }

    private fun isFileDescriptorAwarePlatform(): Boolean =
        OS_NAME.lowercase()
            .let {
                it.startsWith("linux")
                    || it.startsWith("mac")
            }

    private suspend fun awaitStableFileDescriptorCount(
        osBean: UnixOperatingSystemMXBean,
    ): Long {
        var previous = osBean.openFileDescriptorCount

        withTimeoutOrNull(SETTLE_TIMEOUT) {
            while (true) {
                delay(SETTLE_POLL_INTERVAL)
                val current = osBean.openFileDescriptorCount

                if (current == previous) {
                    return@withTimeoutOrNull
                }

                previous = current
            }
        }

        return osBean.openFileDescriptorCount
    }

    private suspend fun awaitFileDescriptorCount(
        osBean: UnixOperatingSystemMXBean,
        threshold: Long,
    ): Long =
        withTimeoutOrNull(SETTLE_TIMEOUT) {
            while (osBean.openFileDescriptorCount > threshold) {
                delay(SETTLE_POLL_INTERVAL)
            }

            return@withTimeoutOrNull osBean.openFileDescriptorCount
        }
            ?: osBean.openFileDescriptorCount

    private suspend fun pingOnce() {
        val pongMessage = KedisClient.newClient(
            configuration = testConfiguration(),
        )
            .use { client ->
                client.execute(
                    command = KedisServerCommands.ping(
                        content = PING_CONTENT,
                    ),
                )
            }

        assertEquals(PING_CONTENT, pongMessage)
    }

    private suspend fun pingOnce(
        selectorManager: SelectorManager,
    ) {
        val pongMessage = KedisClient.newClient(
            configuration = testConfiguration(),
            selectorManager = selectorManager,
        )
            .use { client ->
                client.execute(
                    command = KedisServerCommands.ping(
                        content = PING_CONTENT,
                    ),
                )
            }

        assertEquals(PING_CONTENT, pongMessage)
    }

    private fun testConfiguration(): KedisConfiguration =
        KedisConfiguration(
            endpoint = KedisConfiguration.Endpoint.HostPort(
                host = "127.0.0.1",
                port = TestConfigUtil.getPort(),
            ),
            authentication = KedisConfiguration.Authentication.NoAutoAuth,
            connectionTimeout = CONNECT_TIMEOUT,
        )
}
