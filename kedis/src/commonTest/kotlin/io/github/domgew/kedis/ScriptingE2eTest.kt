package io.github.domgew.kedis

import io.github.domgew.kedis.arguments.server.SyncOption
import io.github.domgew.kedis.commands.KedisScriptingCommands
import io.github.domgew.kedis.commands.KedisServerCommands
import io.github.domgew.kedis.results.scripting.DynamicResult
import io.github.domgew.kedis.utils.TestConfigUtil
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext

class ScriptingE2eTest {

    @Test
    fun test() = runTest {
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

                    assertEquals(
                        DynamicResult.LongResult(1),
                        client.execute(
                            command = KedisScriptingCommands.eval(
                                script = """
                                    return 1
                                """.trimIndent(),
                            ),
                        ),
                    )
                    assertEquals(
                        DynamicResult.BinaryOrStringResult("test"),
                        client.execute(
                            command = KedisScriptingCommands.eval(
                                script = """
                                    return "test"
                                """.trimIndent(),
                            ),
                        ),
                    )
                    assertEquals(
                        DynamicResult.NullResult,
                        client.execute(
                            command = KedisScriptingCommands.eval(
                                script = """
                                    redis.call('SET', KEYS[1], ARGV[1])
                                """.trimIndent(),
                                keys = listOf(
                                    "testKey",
                                ),
                                args = listOf(
                                    "testValue",
                                ),
                            ),
                        ),
                    )
                    assertEquals(
                        DynamicResult.BinaryOrStringResult("OK"),
                        client.execute(
                            command = KedisScriptingCommands.eval(
                                script = """
                                    return redis.call('SET', KEYS[1], ARGV[1])
                                """.trimIndent(),
                                keys = listOf(
                                    "testKey",
                                ),
                                args = listOf(
                                    "testValue",
                                ),
                            ),
                        ),
                    )
                    assertEquals(
                        DynamicResult.BinaryOrStringResult("testValue"),
                        client.execute(
                            command = KedisScriptingCommands.eval(
                                script = """
                                    return redis.call('GET', KEYS[1])
                                """.trimIndent(),
                                keys = listOf(
                                    "testKey",
                                ),
                            ),
                        ),
                    )
                    assertEquals(
                        DynamicResult.NullResult,
                        client.execute(
                            command = KedisScriptingCommands.eval(
                                script = """
                                    return redis.call('GET', KEYS[1])
                                """.trimIndent(),
                                keys = listOf(
                                    "testKey2",
                                ),
                            ),
                        ),
                    )
                }
        }
    }
}
