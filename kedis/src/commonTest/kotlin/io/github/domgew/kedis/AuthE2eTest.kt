package io.github.domgew.kedis

import io.github.domgew.kedis.utils.RedisUtil
import io.github.domgew.kedis.utils.TestConfigUtil
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext

class AuthE2eTest {
    @Test
    fun testAutoAuth() = runTest {
        withContext(Dispatchers.Default) {
            val username = "testUser"
            val password = "testPassword"

            RedisUtil.createUser(
                username = username,
                password = password,
            )

            val client = KedisClient.newClient(
                configuration = KedisConfiguration(
                    endpoint = KedisConfiguration.Endpoint.HostPort(
                        host = "127.0.0.1",
                        port = TestConfigUtil.getPort(),
                    ),
                    authentication = KedisConfiguration.Authentication.AutoAuth(
                        username = username,
                        password = password,
                    ),
                    connectionTimeoutMillis = 2_000L,
                )
            )

            try {
                client.connect()
                assertEquals(username, client.whoAmI())
            } finally {
                RedisUtil.removeUser(
                    username = username,
                )
                client.close()
            }
        }
    }

    @Test
    fun testManualAuth() = runTest {
        withContext(Dispatchers.Default) {
            val username = "testUser"
            val password = "testPassword"

            RedisUtil.createUser(
                username = username,
                password = password,
            )

            val client = KedisClient.newClient(
                configuration = KedisConfiguration(
                    endpoint = KedisConfiguration.Endpoint.HostPort(
                        host = "127.0.0.1",
                        port = TestConfigUtil.getPort(),
                    ),
                    authentication = KedisConfiguration.Authentication.NoAutoAuth,
                    connectionTimeoutMillis = 2_000L,
                )
            )

            try {
                client.connect()
                assertEquals("default", client.whoAmI().lowercase())
                client.auth(
                    username = username,
                    password = password,
                )
                assertEquals(username, client.whoAmI())
            } finally {
                RedisUtil.removeUser(
                    username = username,
                )
                client.close()
            }
        }
    }
}
