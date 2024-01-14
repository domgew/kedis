package io.github.domgew.kedis.results.server

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class InfoSectionTest {
    @Test
    fun testServerSection() {
        val input = """
            # Server
            redis_version:7.2.4
            redis_git_sha1:00000000
            redis_git_dirty:0
            redis_build_id:affe2dab174e19c6
            redis_mode:standalone
            os:Linux 5.15.0-1015-aws x86_64
            arch_bits:64
            monotonic_clock:POSIX clock_gettime
            multiplexing_api:epoll
            atomicvar_api:c11-builtin
            gcc_version:10.2.1
            process_id:1
            process_supervised:no
            run_id:0b562cefe29f5e6be6e8ffcb36b11b6e0abf89a2
            tcp_port:6379
            server_time_usec:1705223769873581
            uptime_in_seconds:419085
            uptime_in_days:4

            hz:10
            configured_hz:10
            lru_clock:10724953
            executable:/data/redis-server
            config_file:/etc/redis/redis.conf
            io_threads_active:0
            listener0:name=tcp,bind=*,bind=-::*,port=6379
            
        """.trimIndent()
        val expected = InfoSection.Server(
            redisVersion = "7.2.4",
            redisGitSha1 = "00000000",
            redisGitDirty = 0,
            redisBuildId = "affe2dab174e19c6",
            redisMode = InfoSection.Server.RedisMode.STANDALONE,
            os = "Linux 5.15.0-1015-aws x86_64",
            archBits = 64,
            multiplexingApi = "epoll",
            atomicVarApi = "c11-builtin",
            gccVersion = "10.2.1",
            processId = 1,
            processSupervised = "no",
            runId = "0b562cefe29f5e6be6e8ffcb36b11b6e0abf89a2",
            tcpPort = 6379,
            serverTimeMicroseconds = 1_705_223_769_873_581u,
            uptimeSeconds = 419_085,
            uptimeDays = 4,
            serverFrequency = 10,
            serverConfiguredFrequency = 10,
            lruClock = 10_724_953u,
            executable = "/data/redis-server",
            configFile = "/etc/redis/redis.conf",
            ioThreadActive = 0,
            shutdownMilliseconds = null,
        )

        val actual = InfoSection.parse(
            InfoSection.parseMap(input),
        )

        assertEquals(1, actual.size)
        assertIs<InfoSection.Server>(actual.first())
        assertEquals(expected, actual.first())
    }

    @Test
    fun testClientsSection() {
        val input = """
            # Clients
            connected_clients:2
            cluster_connections:0
            maxclients:10000
            client_recent_max_input_buffer:20480
            client_recent_max_output_buffer:0
            blocked_clients:0
            tracking_clients:0
            clients_in_timeout_table:0
            total_blocking_keys:0
            total_blocking_keys_on_nokey:0
        """.trimIndent()
        val expected = InfoSection.Clients(
            connectedClients = 2,
            clusterConnections = 0,
            maxClients = 10_000,
            clientRecentMaxInputBuffer = 20_480,
            clientRecentMaxOutputBuffer = 0,
            blockedClients = 0,
            trackingClients = 0,
            pubSubClients = null,
            clientsInTimeoutTable = 0,
            totalBlockingKeys = 0,
            totalBlockingKeysOnNoKey = 0,
        )

        val actual = InfoSection.parse(
            InfoSection.parseMap(input),
        )

        assertEquals(1, actual.size)
        assertIs<InfoSection.Clients>(actual.first())
        assertEquals(expected, actual.first())
    }

    @Test
    fun testMemorySection() {
        val input = """
            # Memory

            used_memory:30063864
            used_memory_human:28.67M
            used_memory_rss:37097472
            used_memory_rss_human:35.38M
            used_memory_peak:30103384
            used_memory_peak_human:28.71M
            used_memory_peak_perc:99.87%
            used_memory_overhead:9053760
            used_memory_startup:867344
            used_memory_dataset:21010104
            used_memory_dataset_perc:71.96%
            allocator_allocated:30148280
            allocator_active:30392320
            allocator_resident:33783808
            total_system_memory:16585551872
            total_system_memory_human:15.45G
            used_memory_lua:31744
            used_memory_vm_eval:31744
            used_memory_lua_human:31.00K
            used_memory_scripts_eval:0
            number_of_cached_scripts:0
            number_of_functions:0
            number_of_libraries:0
            used_memory_vm_functions:32768
            used_memory_vm_total:64512
            used_memory_vm_total_human:63.00K
            used_memory_functions:184
            used_memory_scripts:184
            used_memory_scripts_human:184B
            maxmemory:4294967296
            maxmemory_human:4.00G
            maxmemory_policy:allkeys-lru
            allocator_frag_ratio:1.01
            allocator_frag_bytes:244040
            allocator_rss_ratio:1.11
            allocator_rss_bytes:3391488
            rss_overhead_ratio:1.10
            rss_overhead_bytes:3313664
            mem_fragmentation_ratio:1.23
            mem_fragmentation_bytes:7033808
            mem_not_counted_for_evict:0
            mem_replication_backlog:0
            mem_total_replication_buffers:0
            mem_clients_slaves:0

            mem_clients_normal:24328
            mem_cluster_links:0
            mem_aof_buffer:0

            mem_allocator:jemalloc-5.3.0
            active_defrag_running:0
            lazyfree_pending_objects:0
            lazyfreed_objects:0

        """.trimIndent()
        val expected = InfoSection.Memory(
            usedMemory = 30_063_864u,
            usedMemoryHuman = "28.67M",
            usedMemoryRss = 37_097_472u,
            usedMemoryRssHuman = "35.38M",
            usedMemoryPeak = 30_103_384u,
            usedMemoryPeakHuman = "28.71M",
            usedMemoryPeakPercent = 99.87f,
            usedMemoryOverhead = 9_053_760u,
            usedMemoryStartup = 867_344u,
            usedMemoryDataset = 21_010_104u,
            usedMemoryDatasetPercent = 71.96f,
            totalSystemMemory = 16_585_551_872u,
            totalSystemMemoryHuman = "15.45G",
            usedMemoryLua = 31_744u,
            usedMemoryVmEval = 31_744u,
            usedMemoryLuaHuman = "31.00K",
            usedMemoryScriptsEval = 0u,
            numberOfCachedScripts = 0,
            numberOfFunctions = 0,
            numberOfLibraries = 0,
            usedMemoryVmFunctions = 32_768u,
            usedMemoryVmTotal = 64_512u,
            usedMemoryVmTotalHuman = "63.00K",
            usedMemoryFunctions = 184u,
            usedMemoryScripts = 184u,
            usedMemoryScriptsHuman = "184B",
            maxMemory = 4_294_967_296u,
            maxMemoryHuman = "4.00G",
            maxMemoryPolicy = "allkeys-lru",
            memoryFragmentationRatio = 1.23f,
            memoryFragmentationBytes = 7_033_808u,
            allocatorFragmentationRatio = 1.01f,
            allocatorFragmentationBytes = 244_040u,
            allocatorRssRatio = 1.11f,
            allocatorRssBytes = 3_391_488u,
            rssOverheadRatio = 1.10f,
            rssOverheadBytes = 3_313_664u,
            allocatorAllocated = 30_148_280u,
            allocatorActive = 30_392_320u,
            allocatorResident = 33_783_808u,
            memoryNotCountedForEvict = 0u,
            memoryClientsSlaves = 0u,
            memoryClientsNormal = 24_328u,
            memoryClusterLinks = 0u,
            memoryAofBuffer = 0u,
            memoryReplicationBacklog = 0u,
            memoryTotalReplicationBuffers = 0u,
            memoryAllocator = "jemalloc-5.3.0",
            activeDefragmentationRunning = 0f,
            lazyFreePendingObjects = 0u,
            lazyFreedObjects = 0u,
        )

        val actual = InfoSection.parse(
            InfoSection.parseMap(input),
        )

        assertEquals(1, actual.size)
        assertIs<InfoSection.Memory>(actual.first())
        assertEquals(expected, actual.first())
    }

    @Test
    fun testPersistenceSection() {
        val input = """
            # Persistence
            loading:0
            async_loading:0
            current_cow_peak:0
            current_cow_size:0
            current_cow_size_age:0
            current_fork_perc:0.00
            current_save_keys_processed:0
            current_save_keys_total:0
            rdb_changes_since_last_save:404667
            rdb_bgsave_in_progress:0
            rdb_last_save_time:1704804684
            rdb_last_bgsave_status:ok
            rdb_last_bgsave_time_sec:-1
            rdb_current_bgsave_time_sec:-1
            rdb_saves:0
            rdb_last_cow_size:0
            rdb_last_load_keys_expired:0
            rdb_last_load_keys_loaded:0
            aof_enabled:0
            aof_rewrite_in_progress:0
            aof_rewrite_scheduled:0
            aof_last_rewrite_time_sec:-1
            aof_current_rewrite_time_sec:-1
            aof_last_bgrewrite_status:ok
            aof_rewrites:0
            aof_rewrites_consecutive_failures:0
            aof_last_write_status:ok
            aof_last_cow_size:0
            module_fork_in_progress:0
            module_fork_last_cow_size:0

        """.trimIndent()
        val expected = InfoSection.Persistence(
            loading = 0,
            asyncLoading = "0",
            currentCowPeak = 0,
            currentCowSize = 0,
            currentCowSizeAge = 0,
            currentForkPercent = 0.0f,
            currentSaveKeysProcessed = 0,
            currentSaveKeysTotal = 0,
            rdbChangesSinceLastSave = 404_667,
            rdbBgSaveInProgress = 0,
            rdbLastSaveTime = 1_704_804_684,
            rdbLastBgSaveStatus = "ok",
            rdbLastBgSaveTimeSec = -1,
            rdbCurrentBgSaveTimeSec = -1,
            rdbLastCowSize = 0,
            rdbLastLoadKeysExpired = 0,
            rdbLastLoadKeysLoaded = 0,
            aofEnabled = 0,
            aofRewriteInProgress = 0,
            aofRewriteScheduled = 0,
            aofLastRewriteTimeSec = -1,
            aofCurrentRewriteTimeSec = -1,
            aofLastBgRewriteStatus = "ok",
            aofLastWriteStatus = "ok",
            aofLastCowSize = 0,
            moduleForkInProgress = 0,
            moduleForkLastCowSize = 0,
            aofRewrites = 0,
            rdbSaves = 0,
            aofCurrentSize = null,
            aofBaseSize = null,
            aofPendingRewrite = null,
            aofBufferLength = null,
            aofRewriteBufferLength = null,
            aofPendingBioFSync = null,
            aofDelayedFSync = null,
            loadingStartTime = null,
            loadingTotalBytes = null,
            loadingRdbUsedMemory = null,
            loadingLoadedBytes = null,
            loadingLoadedPercent = null,
            loadingEtaSeconds = null,
        )

        val actual = InfoSection.parse(
            InfoSection.parseMap(input),
        )

        assertEquals(1, actual.size)
        assertIs<InfoSection.Persistence>(actual.first())
        assertEquals(expected, actual.first())
    }
}
