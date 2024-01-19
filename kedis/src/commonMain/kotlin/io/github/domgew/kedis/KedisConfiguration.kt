package io.github.domgew.kedis

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
    val connectionTimeoutMillis: Long,
    /**
     * Whether to keep the socket connection alive.
     */
    val keepAlive: Boolean = false,
    /**
     * The maximum time it may take the server to respond with the full content - this also includes parsing but not waiting for the client lock.
     */
    val readTimeoutMillis: Long? = null,
) {
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
