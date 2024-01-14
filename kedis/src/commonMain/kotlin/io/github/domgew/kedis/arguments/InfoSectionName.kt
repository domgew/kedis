package io.github.domgew.kedis.arguments

/**
 * Info sections you can request from redis.
 * [https://redis.io/commands/info/](https://redis.io/commands/info/)
 */
public enum class InfoSectionName(
    private val redisName: String,
) {
    /**
     * Return all sections (excluding module generated ones)
     */
    ALL("all"),

    /**
     * Return only the default set of sections
     */
    DEFAULT("default"),

    /**
     * Includes *ALL* and *MODULES*
     */
    EVERYTHING("everything"),

    /**
     * General information about the Redis server
     */
    SERVER("server"),

    /**
     * Client connections section
     */
    CLIENTS("clients"),

    /**
     * Memory consumption related information
     */
    MEMORY("memory"),

    /**
     * RDB and AOF related information
     */
    PERSISTENCE("persistence"),

    /**
     * General statistics
     */
    STATS("stats"),

    /**
     * Master/replica replication information
     */
    REPLICATION("replication"),

    /**
     * CPU consumption statistics
     */
    CPU("cpu"),

    /**
     * Redis command statistics
     */
    COMMAND_STATS("commandstats"),

    /**
     * Redis command latency percentile distribution statistics
     */
    LATENCY_STATS("latencystats"),

    /**
     * Redis Sentinel section (only applicable to Sentinel instances)
     */
    SENTINEL("sentinel"),

    /**
     * Redis Cluster section
     */
    CLUSTER("cluster"),

    /**
     * Modules section
     */
    MODULES("modules"),

    /**
     * Database related statistics
     */
    KEY_SPACE("keyspace"),

    /**
     * Redis error statistics
     */
    ERROR_STATS("errorstats"),
    ;

    override fun toString(): String =
        redisName
}
