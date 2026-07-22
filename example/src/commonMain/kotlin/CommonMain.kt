import io.github.domgew.kedis.KedisClient
import io.github.domgew.kedis.arguments.server.SyncOption
import io.github.domgew.kedis.arguments.value.SetOptions
import io.github.domgew.kedis.commands.KedisServerCommands
import io.github.domgew.kedis.commands.KedisValueCommands
import io.github.domgew.kop.KotlinObjectPool
import io.github.domgew.kop.withObject
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
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
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext

fun commonMain() {
    embeddedServer(
        factory = CIO,
        port = 8080,
    ) {
        val kedisPool = KotlinObjectPool.build {
            maxSize(3)
            keepAliveFor(2.minutes)

            createInstance {
                println("kedisPool: Creating new instance")
                KedisClient.builder {
                    hostAndPort("127.0.0.1", 6379)
                    noAutoAuth()
                    connectTimeout = 250.milliseconds
                    keepAlive = true
                }
            }
        }

        routing {
            route("/info") {
                get {
                    call.respondText { "Running" }
                }
            }

            route("/cache/flush") {
                post {
                    kedisPool.withObject { client ->
                        client.execute(
                            command = KedisServerCommands.flushAll(
                                sync = SyncOption.SYNC,
                            ),
                        )
                    }

                    call.respondText {
                        "Flushed"
                    }
                }
            }

            route("/cache/{key}") {
                get {
                    val key = call.getParameterOrFail("key")
                        ?: return@get

                    val fromCache = withContext(Dispatchers.IO) {
                        kedisPool.withObject { client ->
                            client.execute(
                                command = KedisValueCommands.get(
                                    key = key,
                                ),
                            )
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

                    val fromCache = kedisPool.withObject { client ->
                        client.execute(
                            command = KedisValueCommands.set(
                                key = key,
                                call.receiveText(),
                            ),
                        )
                    }

                    call.respondText(
                        status = HttpStatusCode.Accepted,
                    ) {
                        "Accepted: $fromCache"
                    }
                }

                patch {
                    val key = call.getParameterOrFail("key")
                        ?: return@patch

                    val result = kedisPool.withObject {
                        it.getOrCallback(
                            key = key,
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

// you probably want to ensure the connection is fast enough by adding timeouts
private suspend fun KedisClient.getOrCallback(
    key: String,
    ttl: Duration = 1.hours,
    block: suspend () -> String,
): Pair<Boolean, String> {
    val availability = isAvailable()
    if (availability != null) {
        println("KedisClient.getOrCallback: Cache not available: ${availability.message}")
        return Pair(false, block())
    }

    val valueFromCache = try {
        execute(
            command = KedisValueCommands.get(
                key = key,
            ),
        )
    } catch (ex: CancellationException) {
        throw ex
    } catch (ex: Exception) {
        println("KedisClient.getOrCallback: Could not get value from cache: ${ex.message}")
        return Pair(false, block())
    }

    if (valueFromCache != null) {
        return Pair(true, valueFromCache)
    }

    val value = block()

    try {
        execute(
            command = KedisValueCommands.set(
                key = key,
                value = value,
                options = SetOptions(
                    previousKeyHandling = SetOptions.PreviousKeyHandling.OVERRIDE,
                    getPreviousValue = false,
                    expire = SetOptions.ExpireOption.ExpiresInMilliseconds(
                        milliseconds = ttl.inWholeMilliseconds,
                    ),
                ),
            ),
        )
    } catch (ex: Exception) {
        throw ex
    } catch (ex: Exception) {
        println("KedisClient.getOrCallback: Could not write value to cache: ${ex.message}")
    }

    return Pair(false, value)
}

private suspend fun KedisClient.isAvailable(): Throwable? {
    if (isConnected) {
        return null
    }

    try {
        connect()
        return null
    } catch (ex: CancellationException) {
        throw ex
    } catch (ex: Exception) {
        return ex
    }
}

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
