package io.github.domgew.kedis

import io.github.domgew.kedis.arguments.json.JsonGetOptions
import io.github.domgew.kedis.arguments.json.JsonSetOptions
import io.github.domgew.kedis.commands.json.ROOT
import io.github.domgew.kedis.commands.json.jsonGet
import io.github.domgew.kedis.commands.json.plus
import io.github.domgew.kedis.results.value.SetResult
import io.github.domgew.kedis.utils.TestConfigUtil
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@OptIn(ExperimentalSerializationApi::class)
class JsonTest {

    @Serializable
    data class JsonValue(
        @EncodeDefault(EncodeDefault.Mode.ALWAYS)
        val json: String = "json",
        @EncodeDefault(EncodeDefault.Mode.ALWAYS)
        val nested: Nested = Nested()
    ) {

        @Serializable
        data class Nested(
            @EncodeDefault(EncodeDefault.Mode.ALWAYS)
            val nested: String = "nested"
        )

    }

    @Test
    fun test() = runTest {
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

            val key = "Json:Test"
            val value = JsonValue()

            assertEquals(SetResult.Ok,client.jsonSet(key = key,path = "$",value = Json.encodeToString(value),options = JsonSetOptions()))
            assertEquals(Json.encodeToString(listOf(value)),client.jsonGet(key = "Json:Test",path = "$", options = JsonGetOptions()))
            assertEquals(value,client.jsonGet<List<JsonValue>>(key = "Json:Test", path = ROOT, options = JsonGetOptions())!![0])
            assertEquals(value.json,client.jsonGet<List<String>>(key = "Json:Test",path = JsonValue::json,options = JsonGetOptions())!![0])
            assertEquals(value.nested.nested,client.jsonGet<List<String>>(key = "Json:Test",path = JsonValue::nested + JsonValue.Nested::nested,options = JsonGetOptions())!![0])
            client.closeSuspended()
        }
    }


}
