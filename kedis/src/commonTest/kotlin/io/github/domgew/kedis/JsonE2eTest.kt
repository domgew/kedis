package io.github.domgew.kedis

import io.github.domgew.kedis.annotations.RedisModuleJson
import io.github.domgew.kedis.arguments.server.SyncOption
import io.github.domgew.kedis.commands.KedisJsonCommands
import io.github.domgew.kedis.commands.KedisServerCommands
import io.github.domgew.kedis.commands.KedisValueCommands
import io.github.domgew.kedis.results.json.JsonSetResult
import io.github.domgew.kedis.utils.TestConfigUtil
import io.github.domgew.kedis.utils.hasJsonSupport
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext

@OptIn(RedisModuleJson::class)
class JsonE2eTest {

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
                    if (!client.hasJsonSupport()) {
                        return@withContext
                    }

                    val key = "testKey"

                    assertTrue(
                        client.execute(
                            command = KedisServerCommands.flushAll(
                                sync = SyncOption.SYNC,
                            ),
                        ),
                    )

                    assertEquals(
                        0L,
                        client.execute(
                            command = KedisValueCommands.exists(
                                key,
                            ),
                        ),
                    )
                    assertNull(
                        client.execute(
                            command = KedisJsonCommands.jsonGet(
                                key = key,
                            ),
                        ),
                    )
                    assertNull(
                        client.execute(
                            command = KedisJsonCommands.jsonGet(
                                key = key,
                                paths = listOf(
                                    "a",
                                ),
                            ),
                        ),
                    )

                    assertEquals(
                        JsonSetResult.Ok,
                        client.execute(
                            command = KedisJsonCommands.jsonSet(
                                key = key,
                                path = "$",
                                value = """
                                    {
                                        "a": "test",
                                        "b": {
                                            "a": "some"
                                        },
                                        "t": true
                                    }
                                """
                                    .trim()
                                    .replace(
                                        Regex("\\s+"),
                                        " ",
                                    ),
                            ),
                        ),
                    )
                    assertEquals(
                        1L,
                        client.execute(
                            command = KedisValueCommands.exists(
                                key,
                            ),
                        ),
                    )
                    assertEquals(
                        "[\"test\"]",
                        client.execute(
                            KedisJsonCommands.jsonGet(
                                key = key,
                                indent = "",
                                newLine = "",
                                space = "",
                                paths = listOf(
                                    "$.a",
                                ),
                            ),
                        ),
                    )
                    assertEquals(
                        listOf("string"),
                        client.execute(
                            command = KedisJsonCommands.jsonType(
                                key = key,
                                path = "$.a",
                            ),
                        ),
                    )
                    assertEquals(
                        listOf("object"),
                        client.execute(
                            command = KedisJsonCommands.jsonType(
                                key = key,
                                path = "$.b",
                            ),
                        ),
                    )
                    assertEquals(
                        listOf("boolean"),
                        client.execute(
                            command = KedisJsonCommands.jsonType(
                                key = key,
                                path = "$.t",
                            ),
                        ),
                    )
                    assertEquals(
                        "[{\"a\": \"some\"}]",
                        client.execute(
                            KedisJsonCommands.jsonGet(
                                key = key,
                                indent = "",
                                newLine = "",
                                space = " ",
                                paths = listOf(
                                    "$.b",
                                ),
                            ),
                        ),
                    )
                    assertEquals(
                        "[\"some\"]",
                        client.execute(
                            KedisJsonCommands.jsonGet(
                                key = key,
                                indent = "",
                                newLine = "",
                                space = " ",
                                paths = listOf(
                                    "$.b.a",
                                ),
                            ),
                        ),
                    )
                    assertEquals(
                        JsonSetResult.Ok,
                        client.execute(
                            command = KedisJsonCommands.jsonSet(
                                key = key,
                                path = "$.b.a",
                                value = """
                                    "someOther"
                                """.trimIndent(),
                            ),
                        ),
                    )
                    assertEquals(
                        "[\"someOther\"]",
                        client.execute(
                            KedisJsonCommands.jsonGet(
                                key = key,
                                indent = "",
                                newLine = "",
                                space = " ",
                                paths = listOf(
                                    "$.b.a",
                                ),
                            ),
                        ),
                    )
                    assertEquals(
                        listOf(false),
                        client.execute(
                            command = KedisJsonCommands.jsonToggle(
                                key = key,
                                path = "$.t",
                            ),
                        ),
                    )
                    assertEquals(
                        listOf(null),
                        client.execute(
                            command = KedisJsonCommands.jsonToggle(
                                key = key,
                                path = "$.a",
                            ),
                        ),
                    )
                    assertEquals(
                        1,
                        client.execute(
                            command = KedisJsonCommands.jsonDel(
                                key = key,
                                path = "$.b.a",
                            ),
                        ),
                    )
                    assertEquals(
                        "[]",
                        client.execute(
                            KedisJsonCommands.jsonGet(
                                key = key,
                                indent = "",
                                newLine = "",
                                space = " ",
                                paths = listOf(
                                    "$.b.a",
                                ),
                            ),
                        ),
                    )
                }
        }
    }
}
