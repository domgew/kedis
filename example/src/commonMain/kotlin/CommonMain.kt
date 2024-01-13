import io.github.domgew.kedis.KedisClient
import io.github.domgew.kedis.KedisConfiguration
import io.github.domgew.kedis.arguments.SyncOption
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.call
import io.ktor.server.cio.CIO
import io.ktor.server.engine.embeddedServer
import io.ktor.server.request.receiveText
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
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
                host = "127.0.0.1",
                port = 6379,
                connectionTimeoutMillis = 250,
            )
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
            }
        }
    }
        .start(
            wait = true,
        )
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
