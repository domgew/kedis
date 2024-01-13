package io.github.domgew.kedis

public data class KedisConfiguration(
    /**
     * Host name or IP, where the redis server is reachable
     */
    val host: String,
    /**
     * Host port, where the redis server is reachable
     */
    val port: Int = 6379,
    /**
     * The maximum time it may take to establish a connection with the redis server
     */
    val connectionTimeoutMillis: Long,
    /**
     * Whether to keep the socket connection alive
     */
    val keepAlive: Boolean = false,
    /**
     * The maximum time it may take the server to respond with the full content - this also includes parsing
     */
    val readTimeoutMillis: Long? = null,
)
