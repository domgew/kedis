import io.github.domgew.kedis.KedisClient
import io.github.domgew.kedis.KedisConfiguration
import io.github.domgew.kedis.arguments.SetOptions
import io.github.domgew.kedis.arguments.SyncOption
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.call
import io.ktor.server.cio.CIO
import io.ktor.server.engine.embeddedServer
import io.ktor.server.request.receiveText
import io.ktor.server.response.header
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.patch
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext

@OptIn(ExperimentalStdlibApi::class)
fun commonMain() {
    embeddedServer(
        factory = CIO,
        port = 8080,
    ) {
        val redisClient = KedisClient.newClient(
            configuration = KedisConfiguration(
                endpoint = KedisConfiguration.Endpoint.HostPort(
                    host = "127.0.0.1",
                    port = 6379,
                ),
                authentication = KedisConfiguration.Authentication.NoAutoAuth,
                connectionTimeoutMillis = 250,
            ),
        )

        routing {
            route("/info") {
                get {
                    call.respondText { "Running" }
                }
            }

            route("/cache/flush") {
                post {
                    redisClient.use {
                        it.flushAll(sync = SyncOption.SYNC)
                    }
                }
            }

            route("/cache/{key}") {
                get {
                    val key = call.getParameterOrFail("key")
                        ?: return@get

                    val fromCache = withContext(Dispatchers.IO) {
                        redisClient.use {
                            it.get(key)
                        }
                    }

                    if (fromCache == null) {
                        call.respondNotFound()
                        return@get
                    }

                    call.respondText { fromCache }
                }

                post {
                    val key = call.getParameterOrFail("key")
                        ?: return@post

                    val fromCache = redisClient.use {
                        it.set(key, call.receiveText())
                    }

                    call.respondText(
                        status = HttpStatusCode.Accepted,
                    ) { "Accepted: $fromCache" }
                }

                patch {
                    val key = call.getParameterOrFail("key")
                        ?: return@patch

                    val result = redisClient.use {
                        it.getOrCallback(
                            key = key,
                            overrideIfExists = false,
                        ) {
                            // this would probably be an expensive call to some API or similar
                            call.receiveText()
                        }
                    }

                    call.response.header("X-From-Cache", result.first.toString())

                    call.respondText { result.second }
                }
            }
        }
    }
        .start(
            wait = true,
        )
}

// very, very primitive and bad approach - you probably want to check whether the server is available, fast enough and catch error and provide fallbacks
private suspend fun KedisClient.getOrCallback(
    key: String,
    ttlMillis: Long = 1_000L * 60 * 60, // 1h
    overrideIfExists: Boolean = true,
    block: suspend () -> String,
): Pair<Boolean, String> =
    get(
        key = key,
    )
        ?.let { Pair(true, it) }
        ?: block()
            .also {
                set(
                    key = key,
                    value = it,
                    options = SetOptions(
                        previousKeyHandling =
                        if (overrideIfExists) {
                            SetOptions.PreviousKeyHandling.OVERRIDE
                        } else {
                            SetOptions.PreviousKeyHandling.KEEP_IF_EXISTS
                        },
                        expire = SetOptions.ExpireOption.ExpiresInMilliseconds(
                            milliseconds = ttlMillis,
                        ),
                    ),
                )
            }
            .let { Pair(false, it) }

private suspend fun ApplicationCall.respondNotFound() {
    respondText(
        contentType = ContentType.Text.Plain,
        status = HttpStatusCode.NotFound,
    ) { "Not found" }
}

private suspend fun ApplicationCall.getParameterOrFail(
    parameter: String,
): String? =
    getParameterOrFail(
        parameter = parameter,
    ) {
        it
    }

private suspend fun <T> ApplicationCall.getParameterOrFail(
    parameter: String,
    transform: (v: String) -> T,
): T? {
    val result = parameters[parameter]
        ?.trim()
        ?.ifEmpty { null }
        ?: let {
            respondText(
                contentType = ContentType.Text.Plain,
                status = HttpStatusCode.BadRequest,
            ) {
                "Missing parameter: $parameter"
            }
            null
        }

    return result
        ?.let { transform(it) }
}
