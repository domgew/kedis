package io.github.domgew.kedis

import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

public data class KedisConfiguration(
    /**
     * Endpoint, where the redis server is reachable.
     */
    val endpoint: Endpoint,

    /**
     * Defines the kind of authentication performed by the client to the server.
     */
    val authentication: Authentication,

    /**
     * The maximum time it may take to establish a connection with the redis server.
     */
    val connectionTimeout: Duration,

    /**
     * Whether to keep the socket connection alive.
     */
    val keepAlive: Boolean = true,

    /**
     * Which database to use by index.
     */
    val databaseIndex: Int = 0,
) {

    @Deprecated(
        message = "Use timeout duration",
        replaceWith = ReplaceWith(
            expression = "KedisConfiguration(\n" +
                "    endpoint = endpoint,\n" +
                "    authentication = authentication,\n" +
                "    connectionTimeout = connectionTimeoutMillis.milliseconds,\n" +
                "    keepAlive = keepAlive,\n" +
                ")",
            "kotlin.time.Duration.Companion.milliseconds",
        ),
    )
    public constructor(
        endpoint: Endpoint,
        authentication: Authentication,
        connectionTimeoutMillis: Long,
        keepAlive: Boolean = false,
        @Suppress("UNUSED_PARAMETER")
        readTimeoutMillis: Long? = null,
    ) : this(
        endpoint = endpoint,
        authentication = authentication,
        connectionTimeout = connectionTimeoutMillis.milliseconds,
        keepAlive = keepAlive,
    )

    public sealed interface Endpoint {

        public data class HostPort(
            /**
             * Host name or IP, where the redis server is reachable.
             */
            val host: String,
            /**
             * Host port, where the redis server is reachable.
             */
            val port: Int = 6379,
        ) : Endpoint

        public data class UnixSocket(
            /**
             * The path of the UNIX socket where the redis server is reachable.
             */
            val path: String,
        ) : Endpoint
    }

    public sealed interface Authentication {

        /**
         * No automatic authentication is performed when the connection is established. You can do so yourself at any point.
         * @see KedisClient.auth
         */
        public data object NoAutoAuth : Authentication

        /**
         * Authentication is automatically performed after establishing a connection.
         */
        public data class AutoAuth(
            val password: String,
            val username: String? = null,
        ) : Authentication
    }
}
