package io.github.domgew.kedis

import io.ktor.network.selector.SelectorManager
import kotlin.time.Duration

@KedisDsl
public class KedisBuilder internal constructor() {

    public class MissingConfig internal constructor(
        /**
         * Which configuration property was missing.
         *
         * @see KedisConfiguration
         */
        public val what: String,
    ) : Exception(
        "Missing configuration for $what",
    )

    /**
     * Configure the client's host and port.
     *
     * @see KedisConfiguration.Endpoint.HostPort
     */
    public fun hostAndPort(
        host: String,
        port: Int = 6379,
    ): KedisBuilder =
        this
            .also {
                _endpoint = KedisConfiguration.Endpoint.HostPort(
                    host = host,
                    port = port,
                )
            }

    /**
     * Configure the client's UNIX socket.
     *
     * @see KedisConfiguration.Endpoint.HostPort
     */
    public fun unixSocket(
        path: String,
    ): KedisBuilder =
        this
            .also {
                _endpoint = KedisConfiguration.Endpoint.UnixSocket(
                    path = path,
                )
            }

    /**
     * Removes the client's authentication.
     *
     * @see KedisConfiguration.Authentication.NoAutoAuth
     */
    public fun noAutoAuth(): KedisBuilder =
        this
            .also {
                _authentication = KedisConfiguration.Authentication.NoAutoAuth
            }

    /**
     * Configure the client's authentication.
     *
     * @see KedisConfiguration.Authentication.AutoAuth
     */
    public fun autoAuth(
        password: String,
        username: String? = null,
    ): KedisBuilder =
        this
            .also {
                _authentication = KedisConfiguration.Authentication.AutoAuth(
                    password = password,
                    username = username,
                )
            }

    /**
     * Configure the client's connect timeout.
     *
     * @see KedisConfiguration.connectionTimeout
     */
    public var connectTimeout: Duration
        get() =
            throw NotImplementedError("Write only")
        set(value) {
            _connectTimeout = value
        }

    /**
     * Configure the client socket's keep alive.
     *
     * @see KedisConfiguration.keepAlive
     */
    public var keepAlive: Boolean
        get() =
            throw NotImplementedError("Write only")
        set(value) {
            _keepAlive = value
        }

    /**
     * Configure the client's default database index.
     *
     * @see KedisConfiguration.databaseIndex
     */
    public var databaseIndex: Int
        get() =
            throw NotImplementedError("Write only")
        set(value) {
            _databaseIndex = value
        }

    /**
     * Configure the client's selector manager.
     *
     * The selector manager is not closed automatically.
     */
    public var selectorManager: SelectorManager
        get() =
            throw NotImplementedError("Write only")
        set(value) {
            _selectorManager = value
        }

    private var _endpoint: KedisConfiguration.Endpoint? =
        null
    private var _authentication: KedisConfiguration.Authentication =
        KedisConfiguration.Authentication.NoAutoAuth
    private var _connectTimeout: Duration? =
        null
    private var _keepAlive: Boolean =
        true
    private var _databaseIndex: Int =
        0
    private var _selectorManager: SelectorManager? =
        null

    internal fun build(): KedisClient {
        val configuration = KedisConfiguration(
            endpoint = _endpoint
                ?: throw MissingConfig(
                    what = "endpoint",
                ),
            authentication = _authentication,
            connectionTimeout = _connectTimeout
                ?: throw MissingConfig(
                    what = "connectTimeout",
                ),
            keepAlive = _keepAlive,
            databaseIndex = _databaseIndex,
        )

        return when (
            val selectorManager = _selectorManager
        ) {
            null ->
                KedisClient.newClient(
                    configuration = configuration,
                )

            else ->
                KedisClient.newClient(
                    configuration = configuration,
                    selectorManager = selectorManager,
                )
        }
    }
}
