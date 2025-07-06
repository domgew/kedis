package io.github.domgew.kedis

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext

class KedisBuilderTest {

    @Test
    fun test() = runTest {
        withContext(Dispatchers.Default) {
            assertEquals(
                "endpoint",
                assertFailsWith<KedisBuilder.MissingConfig> {
                    KedisClient.builder {}
                }
                    .what,
            )

            assertEquals(
                "connectTimeout",
                assertFailsWith<KedisBuilder.MissingConfig> {
                    KedisClient.builder {
                        hostAndPort("localhost", 1234)
                    }
                }
                    .what,
            )

            KedisClient.builder {
                hostAndPort("localhost", 1234)
                connectTimeout = 250.milliseconds
            }
        }
    }
}
