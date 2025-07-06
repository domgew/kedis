package io.github.domgew.kedis

import io.github.domgew.kedis.commands.KedisServerCommands
import io.github.domgew.kedis.utils.TestConfigUtil
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext

class SelectE2eTest {

    @Test
    fun testSelect() = runTest {
        withContext(Dispatchers.Default) {
            KedisClient.newClient(
                configuration = KedisConfiguration(
                    endpoint = KedisConfiguration.Endpoint.HostPort(
                        host = "127.0.0.1",
                        port = TestConfigUtil.getPort(),
                    ),
                    authentication = KedisConfiguration.Authentication.NoAutoAuth,
                    connectionTimeout = 2.seconds,
                    databaseIndex = 1,
                ),
            )
                .use { client ->
                    assertEquals(0, client.selectedDatabase)
                    client.connect()
                    assertEquals(1, client.selectedDatabase)
                }
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
                    assertEquals(0, client.selectedDatabase)
                    client.connect()
                    assertEquals(0, client.selectedDatabase)
                    client.execute(
                        command = KedisServerCommands.select(
                            databaseIndex = 1,
                        ),
                    )
                    assertEquals(1, client.selectedDatabase)
                    client.execute(
                        command = KedisServerCommands.select(
                            databaseIndex = 0,
                        ),
                    )
                    assertEquals(0, client.selectedDatabase)
                }
        }
    }
}
