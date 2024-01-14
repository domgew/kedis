package io.github.domgew.kedis.results.server

import io.github.domgew.kedis.arguments.InfoSectionName

/**
 * The info section returned by the info command.
 *
 * [https://redis.io/commands/info/](https://redis.io/commands/info/)
 */
public sealed class InfoSection {
    public abstract val sectionName: InfoSectionName

    internal interface InfoSectionResolver {
        @Suppress("PropertyName")
        val SECTION_NAME: String
        fun fromValues(values: Map<String, String>): InfoSection
    }

    public data class Server internal constructor(
        /**
         * **redis_version**: Version of the Redis server
         */
        val redisVersion: String?,
        /**
         * **redis_git_sha1**: Git SHA1
         */
        val redisGitSha1: String?,
        /**
         * **redis_git_dirty**: Git dirty flag
         */
        val redisGitDirty: Int?,
        /**
         * **redis_build_id**: The build id
         */
        val redisBuildId: String?,
        /**
         * **redis_mode**: The server's mode ("standalone", "sentinel" or "cluster")
         */
        val redisMode: RedisMode?,
        /**
         * **os**: Operating system hosting the Redis server
         */
        val os: String?,
        /**
         * **arch_bits**: Architecture (32 or 64 bits)
         */
        val archBits: Int?,
        /**
         * **multiplexing_api**: Event loop mechanism used by Redis
         */
        val multiplexingApi: String?,
        /**
         * **atomicvar_api**: Atomicvar API used by Redis
         */
        val atomicVarApi: String?,
        /**
         * **gcc_version**: Version of the GCC compiler used to compile the Redis server
         */
        val gccVersion: String?,
        /**
         * **process_id**: PID of the server process
         */
        val processId: Int?,
        /**
         * **process_supervised**: Supervised system ("upstart", "systemd", "unknown" or "no")
         */
        val processSupervised: String?,
        /**
         * **run_id**: Random value identifying the Redis server (to be used by Sentinel and Cluster)
         */
        val runId: String?,
        /**
         * **tcp_port**: TCP/IP listen port
         */
        val tcpPort: Int?,
        /**
         * **server_time_usec**: Epoch-based system time with microsecond precision
         */
        val serverTimeMicroseconds: ULong?,
        /**
         * **uptime_in_seconds**: Number of seconds since Redis server start
         */
        val uptimeSeconds: Long?,
        /**
         * **uptime_in_days**: Same value expressed in days
         */
        val uptimeDays: Int?,
        /**
         * **hz**: The server's current frequency setting
         */
        val serverFrequency: Int?,
        /**
         * **configured_hz**: The server's configured frequency setting
         */
        val serverConfiguredFrequency: Int?,
        /**
         * **lru_clock**: Clock incrementing every minute, for LRU management
         */
        val lruClock: ULong?,
        /**
         * **executable**: The path to the server's executable
         */
        val executable: String?,
        /**
         * **config_file**: The path to the config file
         */
        val configFile: String?,
        /**
         * **io_threads_active**: Flag indicating if I/O threads are active
         */
        val ioThreadActive: Int?,
        /**
         * **shutdown_in_milliseconds**: The maximum time remaining for replicas to catch up the replication before completing the shutdown sequence. This field is only present during shutdown.
         */
        val shutdownMilliseconds: Long?,
    ): InfoSection() {
        override val sectionName: InfoSectionName = InfoSectionName.SERVER

        public enum class RedisMode(
            private val redisValue: String,
        ) {
            STANDALONE("standalone"),
            SENTINEL("sentinel"),
            CLUSTER("cluster"),
            ;

            internal companion object {
                fun parseOrNull(value: String) =
                    entries.firstOrNull {
                        it.redisValue == value
                    }
            }
        }

        internal companion object: InfoSectionResolver {
            override val SECTION_NAME = "server"

            override fun fromValues(values: Map<String, String>) =
                Server(
                    redisVersion = values["redis_version"],
                    redisGitSha1 = values["redis_git_sha1"],
                    redisGitDirty = values["redis_git_dirty"]
                        ?.toIntOrNull(),
                    redisBuildId = values["redis_build_id"],
                    redisMode = values["redis_mode"]
                        ?.let { RedisMode.parseOrNull(it) },
                    os = values["os"],
                    archBits = values["arch_bits"]
                        ?.toIntOrNull(),
                    multiplexingApi = values["multiplexing_api"],
                    atomicVarApi = values["atomicvar_api"],
                    gccVersion = values["gcc_version"],
                    processId = values["process_id"]
                        ?.toIntOrNull(),
                    processSupervised = values["process_supervised"],
                    runId = values["run_id"],
                    tcpPort = values["tcp_port"]
                        ?.toIntOrNull(),
                    serverTimeMicroseconds = values["server_time_usec"]
                        ?.toULongOrNull(),
                    uptimeSeconds = values["uptime_in_seconds"]
                        ?.toLongOrNull(),
                    uptimeDays = values["uptime_in_days"]
                        ?.toIntOrNull(),
                    serverFrequency = values["hz"]
                        ?.toIntOrNull(),
                    serverConfiguredFrequency = values["configured_hz"]
                        ?.toIntOrNull(),
                    lruClock = values["lru_clock"]
                        ?.toULongOrNull(),
                    executable = values["executable"],
                    configFile = values["config_file"],
                    ioThreadActive = values["io_threads_active"]
                        ?.toIntOrNull(),
                    shutdownMilliseconds = values["shutdown_in_milliseconds"]
                        ?.toLongOrNull(),
                )
        }
    }

    public data class Clients internal constructor(
        /**
         * **connected_clients**: Number of client connections (excluding connections from replicas)
         */
        val connectedClients: Long?,
        /**
         * **cluster_connections**: An approximation of the number of sockets used by the cluster's bus
         */
        val clusterConnections: Long?,
        /**
         * **maxclients**: The value of the maxclients configuration directive. This is the upper limit for the sum of connected_clients, connected_slaves and cluster_connections.
         */
        val maxClients: Long?,
        /**
         * **client_recent_max_input_buffer**: Biggest input buffer among current client connections
         */
        val clientRecentMaxInputBuffer: Long?,
        /**
         * **client_recent_max_output_buffer**: Biggest output buffer among current client connections
         */
        val clientRecentMaxOutputBuffer: Long?,
        /**
         * **blocked_clients**: Number of clients pending on a blocking call (BLPOP, BRPOP, BRPOPLPUSH, BLMOVE, BZPOPMIN, BZPOPMAX)
         */
        val blockedClients: Long?,
        /**
         * **tracking_clients**: Number of clients being tracked (CLIENT TRACKING)
         */
        val trackingClients: Long?,
        /**
         * **pubsub_clients**: Number of clients in pubsub mode (SUBSCRIBE, PSUBSCRIBE, SSUBSCRIBE). Added in Redis 8.0
         */
        val pubSubClients: Long?,
        /**
         * **clients_in_timeout_table**: Number of clients in the clients timeout table
         */
        val clientsInTimeoutTable: Long?,
        /**
         * **total_blocking_keys**: Number of blocking keys. Added in Redis 7.2.
         */
        val totalBlockingKeys: Long?,
        /**
         * **total_blocking_keys_on_nokey**: Number of blocking keys that one or more clients that would like to be unblocked when the key is deleted. Added in Redis 7.2.
         */
        val totalBlockingKeysOnNoKey: Long?,
    ): InfoSection() {
        override val sectionName: InfoSectionName = InfoSectionName.CLIENTS

        internal companion object: InfoSectionResolver {
            override val SECTION_NAME = "clients"

            override fun fromValues(values: Map<String, String>) =
                Clients(
                    connectedClients = values["connected_clients"]
                        ?.toLongOrNull(),
                    clusterConnections = values["cluster_connections"]
                        ?.toLongOrNull(),
                    maxClients = values["maxclients"]
                        ?.toLongOrNull(),
                    clientRecentMaxInputBuffer = values["client_recent_max_input_buffer"]
                        ?.toLongOrNull(),
                    clientRecentMaxOutputBuffer = values["client_recent_max_output_buffer"]
                        ?.toLongOrNull(),
                    blockedClients = values["blocked_clients"]
                        ?.toLongOrNull(),
                    trackingClients = values["tracking_clients"]
                        ?.toLongOrNull(),
                    pubSubClients = values["pubsub_clients"]
                        ?.toLongOrNull(),
                    clientsInTimeoutTable = values["clients_in_timeout_table"]
                        ?.toLongOrNull(),
                    totalBlockingKeys = values["total_blocking_keys"]
                        ?.toLongOrNull(),
                    totalBlockingKeysOnNoKey = values["total_blocking_keys_on_nokey"]
                        ?.toLongOrNull(),
                )
        }
    }

    public data class Memory internal constructor(
        /**
         * **used_memory**: Total number of bytes allocated by Redis using its allocator (either standard libc, jemalloc, or an alternative allocator such as tcmalloc)
         */
        val usedMemory: ULong?,
        /**
         * **used_memory_human**: Human readable representation of previous value
         */
        val usedMemoryHuman: String?,
        /**
         * **used_memory_rss**: Number of bytes that Redis allocated as seen by the operating system (a.k.a resident set size). This is the number reported by tools such as top(1) and ps(1)
         */
        val usedMemoryRss: ULong?,
        /**
         * **used_memory_rss_human**: Human readable representation of previous value
         */
        val usedMemoryRssHuman: String?,
        /**
         * **used_memory_peak**: Peak memory consumed by Redis (in bytes)
         */
        val usedMemoryPeak: ULong?,
        /**
         * **used_memory_peak_human**: Human readable representation of previous value
         */
        val usedMemoryPeakHuman: String?,
        /**
         * **used_memory_peak_perc**: The percentage of used_memory_peak out of used_memory
         */
        val usedMemoryPeakPercent: Float?,
        /**
         * **used_memory_overhead**: The sum in bytes of all overheads that the server allocated for managing its internal data structures
         */
        val usedMemoryOverhead: ULong?,
        /**
         * **used_memory_startup**: Initial amount of memory consumed by Redis at startup in bytes
         */
        val usedMemoryStartup: ULong?,
        /**
         * **used_memory_dataset**: The size in bytes of the dataset (used_memory_overhead subtracted from used_memory)
         */
        val usedMemoryDataset: ULong?,
        /**
         * **used_memory_dataset_perc**: The percentage of used_memory_dataset out of the net memory usage (used_memory minus used_memory_startup)
         */
        val usedMemoryDatasetPercent: Float?,
        /**
         * **total_system_memory**: The total amount of memory that the Redis host has
         */
        val totalSystemMemory: ULong?,
        /**
         * **total_system_memory_human**: Human readable representation of previous value
         */
        val totalSystemMemoryHuman: String?,
        /**
         * **used_memory_lua**: Number of bytes used by the Lua engine for EVAL scripts. Deprecated in Redis 7.0, renamed to used_memory_vm_eval
         */
        val usedMemoryLua: ULong?,
        /**
         * **used_memory_vm_eval**: Number of bytes used by the script VM engines for EVAL framework (not part of used_memory). Added in Redis 7.0
         */
        val usedMemoryVmEval: ULong?,
        /**
         * **used_memory_lua_human**: Human readable representation of previous value. Deprecated in Redis 7.0
         */
        val usedMemoryLuaHuman: String?,
        /**
         * **used_memory_scripts_eval**: Number of bytes overhead by the EVAL scripts (part of used_memory). Added in Redis 7.0
         */
        val usedMemoryScriptsEval: ULong?,
        /**
         * **number_of_cached_scripts**: The number of EVAL scripts cached by the server. Added in Redis 7.0
         */
        val numberOfCachedScripts: Long?,
        /**
         * **number_of_functions**: The number of functions. Added in Redis 7.0
         */
        val numberOfFunctions: Long?,
        /**
         * **number_of_libraries**: The number of libraries. Added in Redis 7.0
         */
        val numberOfLibraries: Long?,
        /**
         * **used_memory_vm_functions**: Number of bytes used by the script VM engines for Functions framework (not part of used_memory). Added in Redis 7.0
         */
        val usedMemoryVmFunctions: ULong?,
        /**
         * **used_memory_vm_total**: used_memory_vm_eval + used_memory_vm_functions (not part of used_memory). Added in Redis 7.0
         */
        val usedMemoryVmTotal: ULong?,
        /**
         * **used_memory_vm_total_human**: Human readable representation of previous value.
         */
        val usedMemoryVmTotalHuman: String?,
        /**
         * **used_memory_functions**: Number of bytes overhead by Function scripts (part of used_memory). Added in Redis 7.0
         */
        val usedMemoryFunctions: ULong?,
        /**
         * **used_memory_scripts**: used_memory_scripts_eval + used_memory_functions (part of used_memory). Added in Redis 7.0
         */
        val usedMemoryScripts: ULong?,
        /**
         * **used_memory_scripts_human**: Human readable representation of previous value
         */
        val usedMemoryScriptsHuman: String?,
        /**
         * **maxmemory**: The value of the maxmemory configuration directive
         */
        val maxMemory: ULong?,
        /**
         * **maxmemory_human**: Human readable representation of previous value
         */
        val maxMemoryHuman: String?,
        /**
         * **maxmemory_policy**: The value of the maxmemory-policy configuration directive
         */
        val maxMemoryPolicy: String?,
        /**
         * **mem_fragmentation_ratio**: Ratio between used_memory_rss and used_memory. Note that this doesn't only includes fragmentation, but also other process overheads (see the allocator_* metrics), and also overheads like code, shared libraries, stack, etc.
         */
        val memoryFragmentationRatio: Float?,
        /**
         * **mem_fragmentation_bytes**: Delta between used_memory_rss and used_memory. Note that when the total fragmentation bytes is low (few megabytes), a high ratio (e.g. 1.5 and above) is not an indication of an issue.
         */
        val memoryFragmentationBytes: ULong?,
        /**
         * **allocator_frag_ratio**:: Ratio between allocator_active and allocator_allocated. This is the true (external) fragmentation metric (not mem_fragmentation_ratio).
         */
        val allocatorFragmentationRatio: Float?,
        /**
         * **allocator_frag_bytes** Delta between allocator_active and allocator_allocated. See note about mem_fragmentation_bytes.
         */
        val allocatorFragmentationBytes: ULong?,
        /**
         * **allocator_rss_ratio**: Ratio between allocator_resident and allocator_active. This usually indicates pages that the allocator can and probably will soon release back to the OS.
         */
        val allocatorRssRatio: Float?,
        /**
         * **allocator_rss_bytes**: Delta between allocator_resident and allocator_active
         */
        val allocatorRssBytes: ULong?,
        /**
         * **rss_overhead_ratio**: Ratio between used_memory_rss (the process RSS) and allocator_resident. This includes RSS overheads that are not allocator or heap related.
         */
        val rssOverheadRatio: Float?,
        /**
         * **rss_overhead_bytes**: Delta between used_memory_rss (the process RSS) and allocator_resident
         */
        val rssOverheadBytes: ULong?,
        /**
         * **allocator_allocated**: Total bytes allocated form the allocator, including internal-fragmentation. Normally the same as used_memory.
         */
        val allocatorAllocated: ULong?,
        /**
         * **allocator_active**: Total bytes in the allocator active pages, this includes external-fragmentation.
         */
        val allocatorActive: ULong?,
        /**
         * **allocator_resident**: Total bytes resident (RSS) in the allocator, this includes pages that can be released to the OS (by MEMORY PURGE, or just waiting).
         */
        val allocatorResident: ULong?,
        /**
         * **mem_not_counted_for_evict**: Used memory that's not counted for key eviction. This is basically transient replica and AOF buffers.
         */
        val memoryNotCountedForEvict: ULong?,
        /**
         * **mem_clients_slaves**: Memory used by replica clients - Starting Redis 7.0, replica buffers share memory with the replication backlog, so this field can show 0 when replicas don't trigger an increase of memory usage.
         */
        val memoryClientsSlaves: ULong?,
        /**
         * **mem_clients_normal**: Memory used by normal clients
         */
        val memoryClientsNormal: ULong?,
        /**
         * **mem_cluster_links**: Memory used by links to peers on the cluster bus when cluster mode is enabled.
         */
        val memoryClusterLinks: ULong?,
        /**
         * **mem_aof_buffer**: Transient memory used for AOF and AOF rewrite buffers
         */
        val memoryAofBuffer: ULong?,
        /**
         * **mem_replication_backlog**: Memory used by replication backlog
         */
        val memoryReplicationBacklog: ULong?,
        /**
         * **mem_total_replication_buffers**: Total memory consumed for replication buffers - Added in Redis 7.0.
         */
        val memoryTotalReplicationBuffers: ULong?,
        /**
         * **mem_allocator**: Memory allocator, chosen at compile time.
         */
        val memoryAllocator: String?,
        /**
         * **active_defrag_running**: When activedefrag is enabled, this indicates whether defragmentation is currently active, and the CPU percentage it intends to utilize.
         */
        val activeDefragmentationRunning: Float?,
        /**
         * **lazyfree_pending_objects**: The number of objects waiting to be freed (as a result of calling UNLINK, or FLUSHDB and FLUSHALL with the ASYNC option)
         */
        val lazyFreePendingObjects: ULong?,
        /**
         * **lazyfreed_objects**: The number of objects that have been lazy freed.
         */
        val lazyFreedObjects: ULong?,
    ): InfoSection() {
        override val sectionName: InfoSectionName = InfoSectionName.MEMORY

        internal companion object: InfoSectionResolver {
            override val SECTION_NAME = "memory"

            override fun fromValues(values: Map<String, String>) =
                Memory(
                    usedMemory = values["used_memory"]
                        ?.toULongOrNull(),
                    usedMemoryHuman = values["used_memory_human"],
                    usedMemoryRss = values["used_memory_rss"]
                        ?.toULongOrNull(),
                    usedMemoryRssHuman = values["used_memory_rss_human"],
                    usedMemoryPeak = values["used_memory_peak"]
                        ?.toULongOrNull(),
                    usedMemoryPeakHuman = values["used_memory_peak_human"],
                    usedMemoryPeakPercent = values["used_memory_peak_perc"]
                        ?.parsePercent(),
                    usedMemoryOverhead = values["used_memory_overhead"]
                        ?.toULongOrNull(),
                    usedMemoryStartup = values["used_memory_startup"]
                        ?.toULongOrNull(),
                    usedMemoryDataset = values["used_memory_dataset"]
                        ?.toULongOrNull(),
                    usedMemoryDatasetPercent = values["used_memory_dataset_perc"]
                        ?.parsePercent(),
                    totalSystemMemory = values["total_system_memory"]
                        ?.toULongOrNull(),
                    totalSystemMemoryHuman = values["total_system_memory_human"],
                    usedMemoryLua = values["used_memory_lua"]
                        ?.toULongOrNull(),
                    usedMemoryVmEval = values["used_memory_vm_eval"]
                        ?.toULongOrNull(),
                    usedMemoryLuaHuman = values["used_memory_lua_human"],
                    usedMemoryScriptsEval = values["used_memory_scripts_eval"]
                        ?.toULongOrNull(),
                    numberOfCachedScripts = values["number_of_cached_scripts"]
                        ?.toLongOrNull(),
                    numberOfFunctions = values["number_of_functions"]
                        ?.toLongOrNull(),
                    numberOfLibraries = values["number_of_libraries"]
                        ?.toLongOrNull(),
                    usedMemoryVmFunctions = values["used_memory_vm_functions"]
                        ?.toULongOrNull(),
                    usedMemoryVmTotal = values["used_memory_vm_total"]
                        ?.toULongOrNull(),
                    usedMemoryVmTotalHuman = values["used_memory_vm_total_human"],
                    usedMemoryFunctions = values["used_memory_functions"]
                        ?.toULongOrNull(),
                    usedMemoryScripts = values["used_memory_scripts"]
                        ?.toULongOrNull(),
                    usedMemoryScriptsHuman = values["used_memory_scripts_human"],
                    maxMemory = values["maxmemory"]
                        ?.toULongOrNull(),
                    maxMemoryHuman = values["maxmemory_human"],
                    maxMemoryPolicy = values["maxmemory_policy"],
                    memoryFragmentationRatio = values["mem_fragmentation_ratio"]
                        ?.toFloatOrNull(),
                    memoryFragmentationBytes = values["mem_fragmentation_bytes"]
                        ?.toULongOrNull(),
                    allocatorFragmentationRatio = values["allocator_frag_ratio"]
                        ?.toFloatOrNull(),
                    allocatorFragmentationBytes = values["allocator_frag_bytes"]
                        ?.toULongOrNull(),
                    allocatorRssRatio = values["allocator_rss_ratio"]
                        ?.toFloatOrNull(),
                    allocatorRssBytes = values["allocator_rss_bytes"]
                        ?.toULongOrNull(),
                    rssOverheadRatio = values["rss_overhead_ratio"]
                        ?.toFloatOrNull(),
                    rssOverheadBytes = values["rss_overhead_bytes"]
                        ?.toULongOrNull(),
                    allocatorAllocated = values["allocator_allocated"]
                        ?.toULongOrNull(),
                    allocatorActive = values["allocator_active"]
                        ?.toULongOrNull(),
                    allocatorResident = values["allocator_resident"]
                        ?.toULongOrNull(),
                    memoryNotCountedForEvict = values["mem_not_counted_for_evict"]
                        ?.toULongOrNull(),
                    memoryClientsSlaves = values["mem_clients_slaves"]
                        ?.toULongOrNull(),
                    memoryClientsNormal = values["mem_clients_normal"]
                        ?.toULongOrNull(),
                    memoryClusterLinks = values["mem_cluster_links"]
                        ?.toULongOrNull(),
                    memoryAofBuffer = values["mem_aof_buffer"]
                        ?.toULongOrNull(),
                    memoryReplicationBacklog = values["mem_replication_backlog"]
                        ?.toULongOrNull(),
                    memoryTotalReplicationBuffers = values["mem_total_replication_buffers"]
                        ?.toULongOrNull(),
                    memoryAllocator = values["mem_allocator"],
                    activeDefragmentationRunning = values["active_defrag_running"]
                        ?.toFloatOrNull(),
                    lazyFreePendingObjects = values["lazyfree_pending_objects"]
                        ?.toULongOrNull(),
                    lazyFreedObjects = values["lazyfreed_objects"]
                        ?.toULongOrNull(),
                )

            private fun String?.parsePercent(): Float? =
                this
                    // check format
                    ?.takeIf { it.endsWith('%') && it.contains('.') }
                    // remove percent sign
                    ?.let { it.substring(0 ..(it.length - 2)) }
                    ?.toFloatOrNull()
        }
    }

    public data class Persistence(
        /**
         * **loading**: Flag indicating if the load of a dump file is on-going
         */
        val loading: Int?,

        /**
         * **async_loading**: Currently loading replication data-set asynchronously while serving old data. This means repl-diskless-load is enabled and set to swapdb. Added in Redis 7.0.
         */
        val asyncLoading: String?,

        /**
         * **current_cow_peak**: The peak size in bytes of copy-on-write memory while a child fork is running
         */
        val currentCowPeak: Long?,

        /**
         * **current_cow_size**: The size in bytes of copy-on-write memory while a child fork is running
         */
        val currentCowSize: Long?,

        /**
         * **current_cow_size_age**: The age, in seconds, of the current_cow_size value.
         */
        val currentCowSizeAge: Long?,

        /**
         * **current_fork_perc**: The percentage of progress of the current fork process. For AOF and RDB forks it is the percentage of current_save_keys_processed out of current_save_keys_total.
         */
        val currentForkPercent: Float?,

        /**
         * **current_save_keys_processed**: Number of keys processed by the current save operation
         */
        val currentSaveKeysProcessed: Long?,

        /**
         * **current_save_keys_total**: Number of keys at the beginning of the current save operation
         */
        val currentSaveKeysTotal: Long?,

        /**
         * **rdb_changes_since_last_save**: Number of changes since the last dump
         */
        val rdbChangesSinceLastSave: Long?,

        /**
         * **rdb_bgsave_in_progress**: Flag indicating a RDB save is on-going
         */
        val rdbBgSaveInProgress: Int?,

        /**
         * **rdb_last_save_time**: Epoch-based timestamp (seconds) of last successful RDB save
         */
        val rdbLastSaveTime: Long?,

        /**
         * **rdb_last_bgsave_status**: Status of the last RDB save operation
         */
        val rdbLastBgSaveStatus: String?,

        /**
         * **rdb_last_bgsave_time_sec**: Duration of the last RDB save operation in seconds
         */
        val rdbLastBgSaveTimeSec: Long?,

        /**
         * **rdb_current_bgsave_time_sec**: Duration of the on-going RDB save operation if any
         */
        val rdbCurrentBgSaveTimeSec: Long?,

        /**
         * **rdb_last_cow_size**: The size in bytes of copy-on-write memory during the last RDB save operation
         */
        val rdbLastCowSize: Long?,

        /**
         * **rdb_last_load_keys_expired**: Number of volatile keys deleted during the last RDB loading. Added in Redis 7.0.
         */
        val rdbLastLoadKeysExpired: Long?,

        /**
         * **rdb_last_load_keys_loaded**: Number of keys loaded during the last RDB loading. Added in Redis 7.0.
         */
        val rdbLastLoadKeysLoaded: Long?,

        /**
         * **aof_enabled**: Flag indicating AOF logging is activated
         */
        val aofEnabled: Int?,

        /**
         * **aof_rewrite_in_progress**: Flag indicating a AOF rewrite operation is on-going
         */
        val aofRewriteInProgress: Int?,

        /**
         * **aof_rewrite_scheduled**: Flag indicating an AOF rewrite operation will be scheduled once the on-going RDB save is complete.
         */
        val aofRewriteScheduled: Int?,

        /**
         * **aof_last_rewrite_time_sec**: Duration of the last AOF rewrite operation in seconds
         */
        val aofLastRewriteTimeSec: Long?,

        /**
         * **aof_current_rewrite_time_sec**: Duration of the on-going AOF rewrite operation if any
         */
        val aofCurrentRewriteTimeSec: Long?,

        /**
         * **aof_last_bgrewrite_status**: Status of the last AOF rewrite operation
         */
        val aofLastBgRewriteStatus: String?,

        /**
         * **aof_last_write_status**: Status of the last write operation to the AOF
         */
        val aofLastWriteStatus: String?,

        /**
         * **aof_last_cow_size**: The size in bytes of copy-on-write memory during the last AOF rewrite operation
         */
        val aofLastCowSize: Long?,

        /**
         * **module_fork_in_progress**: Flag indicating a module fork is on-going
         */
        val moduleForkInProgress: Int?,

        /**
         * **module_fork_last_cow_size**: The size in bytes of copy-on-write memory during the last module fork operation
         */
        val moduleForkLastCowSize: Long?,

        /**
         * **aof_rewrites**: Number of AOF rewrites performed since startup
         */
        val aofRewrites: Long?,

        /**
         * **rdb_saves**: Number of RDB snapshots performed since startup
         */
        val rdbSaves: Long?,

        /**
         * **aof_current_size**: AOF current file size
         */
        val aofCurrentSize: Long?,

        /**
         * **aof_base_size**: AOF file size on latest startup or rewrite
         */
        val aofBaseSize: Long?,

        /**
         * **aof_pending_rewrite**: Flag indicating an AOF rewrite operation will be scheduled once the on-going RDB save is complete.
         */
        val aofPendingRewrite: Int?,

        /**
         * **aof_buffer_length**: Size of the AOF buffer
         */
        val aofBufferLength: Long?,

        /**
         * **aof_rewrite_buffer_length**: Size of the AOF rewrite buffer. Note this field was removed in Redis 7.0
         */
        val aofRewriteBufferLength: Long?,

        /**
         * **aof_pending_bio_fsync**: Number of fsync pending jobs in background I/O queue
         */
        val aofPendingBioFSync: Long?,

        /**
         * **aof_delayed_fsync**: Delayed fsync counter
         */
        val aofDelayedFSync: Long?,

        /**
         * **loading_start_time**: Epoch-based timestamp of the start of the load operation
         */
        val loadingStartTime: Long?,

        /**
         * **loading_total_bytes**: Total file size
         */
        val loadingTotalBytes: Long?,

        /**
         * **loading_rdb_used_mem**: The memory usage of the server that had generated the RDB file at the time of the file's creation
         */
        val loadingRdbUsedMemory: Long?,

        /**
         * **loading_loaded_bytes**: Number of bytes already loaded
         */
        val loadingLoadedBytes: Long?,

        /**
         * **loading_loaded_perc**: Same value expressed as a percentage
         */
        val loadingLoadedPercent: Float?,

        /**
         * **loading_eta_seconds**: ETA in seconds for the load to be complete
         */
        val loadingEtaSeconds: Long?,
    ): InfoSection() {
        override val sectionName: InfoSectionName = InfoSectionName.PERSISTENCE

        internal companion object: InfoSectionResolver {
            override val SECTION_NAME = "persistence"

            override fun fromValues(values: Map<String, String>) =
                Persistence(
                    loading = values["loading"]
                        ?.toIntOrNull(),
                    asyncLoading = values["async_loading"],
                    currentCowPeak = values["current_cow_peak"]
                        ?.toLongOrNull(),
                    currentCowSize = values["current_cow_size"]
                        ?.toLongOrNull(),
                    currentCowSizeAge = values["current_cow_size_age"]
                        ?.toLongOrNull(),
                    currentForkPercent = values["current_fork_perc"]
                        ?.toFloatOrNull(),
                    currentSaveKeysProcessed = values["current_save_keys_processed"]
                        ?.toLongOrNull(),
                    currentSaveKeysTotal = values["current_save_keys_total"]
                        ?.toLongOrNull(),
                    rdbChangesSinceLastSave = values["rdb_changes_since_last_save"]
                        ?.toLongOrNull(),
                    rdbBgSaveInProgress = values["rdb_bgsave_in_progress"]
                        ?.toIntOrNull(),
                    rdbLastSaveTime = values["rdb_last_save_time"]
                        ?.toLongOrNull(),
                    rdbLastBgSaveStatus = values["rdb_last_bgsave_status"],
                    rdbLastBgSaveTimeSec = values["rdb_last_bgsave_time_sec"]
                        ?.toLongOrNull(),
                    rdbCurrentBgSaveTimeSec = values["rdb_current_bgsave_time_sec"]
                        ?.toLongOrNull(),
                    rdbLastCowSize = values["rdb_last_cow_size"]
                        ?.toLongOrNull(),
                    rdbLastLoadKeysExpired = values["rdb_last_load_keys_expired"]
                        ?.toLongOrNull(),
                    rdbLastLoadKeysLoaded = values["rdb_last_load_keys_loaded"]
                        ?.toLongOrNull(),
                    aofEnabled = values["aof_enabled"]
                        ?.toIntOrNull(),
                    aofRewriteInProgress = values["aof_rewrite_in_progress"]
                        ?.toIntOrNull(),
                    aofRewriteScheduled = values["aof_rewrite_scheduled"]
                        ?.toIntOrNull(),
                    aofLastRewriteTimeSec = values["aof_last_rewrite_time_sec"]
                        ?.toLongOrNull(),
                    aofCurrentRewriteTimeSec = values["aof_current_rewrite_time_sec"]
                        ?.toLongOrNull(),
                    aofLastBgRewriteStatus = values["aof_last_bgrewrite_status"],
                    aofLastWriteStatus = values["aof_last_write_status"],
                    aofLastCowSize = values["aof_last_cow_size"]
                        ?.toLongOrNull(),
                    moduleForkInProgress = values["module_fork_in_progress"]
                        ?.toIntOrNull(),
                    moduleForkLastCowSize = values["module_fork_last_cow_size"]
                        ?.toLongOrNull(),
                    aofRewrites = values["aof_rewrites"]
                        ?.toLongOrNull(),
                    rdbSaves = values["rdb_saves"]
                        ?.toLongOrNull(),
                    aofCurrentSize = values["aof_current_size"]
                        ?.toLongOrNull(),
                    aofBaseSize = values["aof_base_size"]
                        ?.toLongOrNull(),
                    aofPendingRewrite = values["aof_pending_rewrite"]
                        ?.toIntOrNull(),
                    aofBufferLength = values["aof_buffer_length"]
                        ?.toLongOrNull(),
                    aofRewriteBufferLength = values["aof_rewrite_buffer_length"]
                        ?.toLongOrNull(),
                    aofPendingBioFSync = values["aof_pending_bio_fsync"]
                        ?.toLongOrNull(),
                    aofDelayedFSync = values["aof_delayed_fsync"]
                        ?.toLongOrNull(),
                    loadingStartTime = values["loading_start_time"]
                        ?.toLongOrNull(),
                    loadingTotalBytes = values["loading_total_bytes"]
                        ?.toLongOrNull(),
                    loadingRdbUsedMemory = values["loading_rdb_used_mem"]
                        ?.toLongOrNull(),
                    loadingLoadedBytes = values["loading_loaded_bytes"]
                        ?.toLongOrNull(),
                    loadingLoadedPercent = values["loading_loaded_perc"]
                        ?.toFloatOrNull(),
                    loadingEtaSeconds = values["loading_eta_seconds"]
                        ?.toLongOrNull(),
                )
        }
    }

    internal companion object {
        internal fun parseMap(response: String): Map<String?, Map<String, String>> {
            val result = HashMap<String?, HashMap<String, String>>()
            val lines = response
                .split('\n')
                .mapNotNull {
                    it.trim()
                        .ifEmpty { null }
                }
            var lineIdx = 0

            // loop over sections
            while (lineIdx < lines.size) {
                val sectionName = lines[lineIdx]
                    // check if it is a section
                    .takeIf { it.startsWith(SECTION_START) }
                    // if it is a section we need to skip the current line for the first value
                    ?.also { lineIdx++ }
                    // first character is section identifier ('#')
                    ?.substring(1)
                    ?.trim()
                    // it is human-readable so casing differs
                    ?.lowercase()
                    ?.ifEmpty { null }
                val sectionValues = result[sectionName] ?: HashMap()

                // only take lines until next section starts or the input string ends
                while (
                    lineIdx < lines.size
                    && !lines[lineIdx].startsWith(SECTION_START)
                ) {
                    val line = lines[lineIdx++]
                    val delimiterIdx = line.indexOf(VALUE_DELIMITER)
                        .takeUnless { it < 0 || it >= line.length }
                        // not a key-value pair
                        ?: continue
                    val key = line.substring(0 until delimiterIdx)
                        .trim()
                        .ifEmpty { null }
                        // empty key
                        ?: continue
                    val value = line.substring(delimiterIdx + 1)
                        .trim()
                        .ifEmpty { null }
                        // empty value
                        ?: continue

                    sectionValues[key] = value
                }

                result[sectionName] = sectionValues
            }

            return result
        }

        internal fun parse(responseMap: Map<String?, Map<String, String>>): List<InfoSection> {
            return responseMap
                .mapNotNull { sectionEntry ->
                    val sectionName = sectionEntry.key
                        ?: return@mapNotNull null
                    val sectionValues = sectionEntry.value
                        .takeIf { it.isNotEmpty() }
                        ?: return@mapNotNull null

                    return@mapNotNull getSectionFromValues(
                        sectionName = sectionName,
                        sectionValues = sectionValues,
                    )
                }
        }

        private fun getSectionFromValues(
            sectionName: String,
            sectionValues: Map<String, String>,
        ): InfoSection? =
            when (sectionName) {
                Server.SECTION_NAME ->
                    Server.fromValues(sectionValues)

                Clients.SECTION_NAME ->
                    Clients.fromValues(sectionValues)

                Memory.SECTION_NAME ->
                    Memory.fromValues(sectionValues)

                Persistence.SECTION_NAME ->
                    Persistence.fromValues(sectionValues)

                // TODO: stats
                // TODO: replication
                // TODO: cpu
                // TODO: commandstats
                // TODO: latencystats
                // TODO: errorstats
                // TODO: sentinel
                // TODO: cluster
                // TODO: modules
                // TODO: keyspace
                // TODO: debug

                // unknown section
                else ->
                    null
            }

        private const val SECTION_START = '#'
        private const val VALUE_DELIMITER = ':'
    }
}
