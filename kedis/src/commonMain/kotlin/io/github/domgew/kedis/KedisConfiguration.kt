package io.github.domgew.kedis

public data class KedisConfiguration(
    /**
     * Endpoint, where the redis server is reachable.
     */
    val endpoint: Endpoint,
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
}
