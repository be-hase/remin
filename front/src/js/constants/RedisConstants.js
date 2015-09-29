var constants = {
    metrics: [
        // client
        {
            name: 'connected_clients',
            desc: 'Number of client connections (excluding connections from slaves).',
            valueType: 'number'
        },
        {
            name: 'client_longest_output_list',
            desc: 'longest output list among current client connections.',
            valueType: 'number'
        },
        {
            name: 'client_biggest_input_buf',
            desc: 'biggest input buffer among current client connections.',
            valueType: 'number'
        },
        {
            name: 'blocked_clients',
            desc: 'Number of clients pending on a blocking call (BLPOP, BRPOP, BRPOPLPUSH).',
            valueType: 'number'
        },
        // memory
        {
            name: 'used_memory',
            desc: 'total number of bytes allocated by Redis using its allocator (either standard libc, jemalloc, or an alternative allocator such as tcmalloc.',
            valueType: 'number'
        },
        {
            name: 'used_memory_rss',
            desc: 'Number of bytes that Redis allocated as seen by the operating system (a.k.a resident set size). This is the number reported by tools such as top and ps..',
            valueType: 'number'
        },
        {
            name: 'used_memory_peak',
            desc: 'Peak memory consumed by Redis (in bytes).',
            valueType: 'number'
        },
        {
            name: 'used_memory_lua',
            desc: 'Number of bytes used by the Lua engine.',
            valueType: 'number'
        },
        {
            name: 'mem_fragmentation_ratio',
            desc: 'Ratio between used_memory_rss and used_memory.',
            valueType: 'number'
        },
        // persistence
        {
            name: 'loading',
            desc: 'Flag indicating if the load of a dump file is on-going.',
            valueType: 'string'
        },
        {
            name: 'rdb_changes_since_last_save',
            desc: 'Number of changes since the last dump.',
            valueType: 'number'
        },
        {
            name: 'rdb_bgsave_in_progress',
            desc: 'Flag indicating a RDB save is on-going.',
            valueType: 'string'
        },
        {
            name: 'rdb_last_save_time',
            desc: 'Epoch-based timestamp of last successful RDB save.',
            valueType: 'number'
        },
        {
            name: 'rdb_last_bgsave_status',
            desc: 'Status of the last RDB save operation.',
            valueType: 'string'
        },
        {
            name: 'rdb_last_bgsave_time_sec',
            desc: 'Duration of the last RDB save operation in seconds.',
            valueType: 'number'
        },
        {
            name: 'rdb_current_bgsave_time_sec',
            desc: 'Duration of the on-going RDB save operation if any.',
            valueType: 'number'
        },
        {
            name: 'aof_rewrite_in_progress',
            desc: 'Flag indicating a AOF rewrite operation is on-going.',
            valueType: 'string'
        },
        {
            name: 'aof_rewrite_scheduled',
            desc: 'Flag indicating an AOF rewrite operation will be scheduled once the on-going RDB save is complete..',
            valueType: 'string'
        },
        {
            name: 'aof_last_rewrite_time_sec',
            desc: 'Duration of the last AOF rewrite operation in seconds.',
            valueType: 'number'
        },
        {
            name: 'aof_current_rewrite_time_sec',
            desc: 'Duration of the on-going AOF rewrite operation if any.',
            valueType: 'number'
        },
        {
            name: 'aof_last_bgrewrite_status',
            desc: 'Status of the last AOF rewrite operation.',
            valueType: 'string'
        },
        {
            name: 'aof_last_write_status',
            desc: 'Status of the last AOF write operation.',
            valueType: 'string'
        },
        {
            name: 'aof_current_size',
            desc: 'AOF current file size.',
            valueType: 'number'
        },
        {
            name: 'aof_base_size',
            desc: 'AOF file size on latest startup or rewrite.',
            valueType: 'number'
        },
        {
            name: 'aof_pending_rewrite',
            desc: 'Flag indicating an AOF rewrite operation will be scheduled once the on-going RDB save is complete..',
            valueType: 'string'
        },
        {
            name: 'aof_buffer_length',
            desc: ' Size of the AOF buffer.',
            valueType: 'number'
        },
        {
            name: 'aof_rewrite_buffer_length',
            desc: 'Size of the AOF rewrite buffer.',
            valueType: 'number'
        },
        {
            name: 'aof_pending_bio_fsync',
            desc: 'Number of fsync pending jobs in background I/O queue.',
            valueType: 'number'
        },
        {
            name: 'aof_delayed_fsync',
            desc: 'Delayed fsync counter.',
            valueType: 'number'
        },
        {
            name: 'loading_start_time',
            desc: 'Epoch-based timestamp of the start of the load operation.',
            valueType: 'number'
        },
        {
            name: 'loading_total_bytes',
            desc: 'Total file size.',
            valueType: 'number'
        },
        {
            name: 'loading_loaded_bytes',
            desc: 'Number of bytes already loaded.',
            valueType: 'number'
        },
        {
            name: 'loading_loaded_perc',
            desc: 'Same value expressed as a percentage.',
            valueType: 'number'
        },
        {
            name: 'loading_eta_seconds',
            desc: 'ETA in seconds for the load to be complete.',
            valueType: 'number'
        },
        // stats
        {
            name: 'total_connections_received',
            desc: 'Total number of connections accepted by the server.',
            valueType: 'number'
        },
        {
            name: 'total_commands_processed',
            desc: 'Total number of commands processed by the server.',
            valueType: 'number'
        },
        {
            name: 'instantaneous_ops_per_sec',
            desc: 'Number of commands processed per second.',
            valueType: 'number'
        },
        {
            name: 'rejected_connections',
            desc: 'Number of connections rejected because of maxclients limit.',
            valueType: 'number'
        },
        {
            name: 'expired_keys',
            desc: 'Total number of key expiration events.',
            valueType: 'number'
        },
        {
            name: 'evicted_keys',
            desc: 'Number of evicted keys due to maxmemory limit.',
            valueType: 'number'
        },
        {
            name: 'keyspace_hits',
            desc: 'Number of successful lookup of keys in the main dictionary.',
            valueType: 'number'
        },
        {
            name: 'keyspace_misses',
            desc: 'Number of failed lookup of keys in the main dictionary.',
            valueType: 'number'
        },
        {
            name: 'pubsub_channels',
            desc: 'Global number of pub/sub channels with client subscriptions.',
            valueType: 'number'
        },
        {
            name: 'pubsub_patterns',
            desc: 'Global number of pub/sub pattern with client subscriptions.',
            valueType: 'number'
        },
        {
            name: 'latest_fork_usec',
            desc: 'Duration of the latest fork operation in microseconds.',
            valueType: 'number'
        },
        // replication
        {
            name: 'master_last_io_seconds_ago',
            desc: 'Number of seconds since the last interaction with master.',
            valueType: 'number'
        },
        {
            name: 'master_sync_in_progress',
            desc: 'Indicate the master is SYNCing to the slave.',
            valueType: 'string'
        },
        {
            name: 'master_sync_left_bytes',
            desc: 'Number of bytes left before SYNCing is complete.',
            valueType: 'number'
        },
        {
            name: 'master_sync_last_io_seconds_ago',
            desc: 'Number of seconds since last transfer I/O during a SYNC operation.',
            valueType: 'number'
        },
        {
            name: 'master_link_down_since_seconds',
            desc: 'Number of seconds since the link is down.',
            valueType: 'number'
        },
        {
            name: 'connected_slaves',
            desc: 'Number of connected slaves.',
            valueType: 'number'
        },
        // cpu
        {
            name: 'used_cpu_sys',
            desc: 'System CPU consumed by the Redis server.',
            valueType: 'number'
        },
        {
            name: 'used_cpu_user',
            desc: 'User CPU consumed by the Redis server.',
            valueType: 'number'
        },
        {
            name: 'used_cpu_sys_children',
            desc: 'System CPU consumed by the background processes.',
            valueType: 'number'
        },
        {
            name: 'used_cpu_user_children',
            desc: 'User CPU consumed by the background processes.',
            valueType: 'number'
        }
    ]
};

module.exports = constants;
