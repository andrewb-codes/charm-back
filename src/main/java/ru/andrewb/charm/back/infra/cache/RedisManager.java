package ru.andrewb.charm.back.infra.cache;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import ru.andrewb.charm.back.config.Config;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

@Slf4j
@UtilityClass
public class RedisManager {

    private static volatile JedisPool pool;
    private static volatile boolean initialized = false;

    // connection
    private static volatile String host;
    private static volatile int port;
    private static volatile int timeoutMs;

    // app settings
    private static volatile int charmQueueTtlSec;
    private static volatile int charmEmptyTtlSec;
    private static volatile int charmLockTtlSec;

    public static synchronized void initOrThrow() {
        if (initialized) return;

        host = Config.required("app.redis.host");
        port = Integer.parseInt(Config.getOrDefault("app.redis.port", "6379"));
        timeoutMs = Integer.parseInt(Config.getOrDefault("app.redis.timeout-ms", "2000"));

        charmQueueTtlSec = Integer.parseInt(Config.getOrDefault("app.redis.charm-queue-ttl-sec", "300"));
        charmEmptyTtlSec = Integer.parseInt(Config.getOrDefault("app.redis.charm-empty-ttl-sec", "60"));
        charmLockTtlSec = Integer.parseInt(Config.getOrDefault("app.redis.charm-lock-ttl-sec", "3"));

        int maxTotal = Integer.parseInt(Config.getOrDefault("app.redis.pool.max-total", "16"));
        int maxIdle = Integer.parseInt(Config.getOrDefault("app.redis.pool.max-idle", "16"));
        int minIdle = Integer.parseInt(Config.getOrDefault("app.redis.pool.min-idle", "0"));
        boolean testOnBorrow = Boolean.parseBoolean(Config.getOrDefault("app.redis.pool.test-on-borrow", "true"));

        try {
            JedisPoolConfig cfg = new JedisPoolConfig();
            cfg.setMaxTotal(maxTotal);
            cfg.setMaxIdle(maxIdle);
            cfg.setMinIdle(minIdle);
            cfg.setTestOnBorrow(testOnBorrow);

            pool = new JedisPool(cfg, host, port, timeoutMs);
            initialized = true;

            log.info("Redis initialized: host={}, port={}, timeoutMs={}, maxTotal={}, maxIdle={}, minIdle={}, testOnBorrow={}",
                    host, port, timeoutMs, maxTotal, maxIdle, minIdle, testOnBorrow);
            log.info("Redis ttl settings: charmQueueTtlSec={}, charmEmptyTtlSec={}, charmLockTtlSec={}",
                    charmQueueTtlSec, charmEmptyTtlSec, charmLockTtlSec);

        } catch (Exception e) {
            log.error("Failed to init Redis: host={}, port={}", host, port, e);
            throw new IllegalStateException("Failed to init Redis", e);
        }
    }

    public static void ensureInit() {
        if (!initialized || pool == null) {
            throw new IllegalStateException("RedisManager is not initialized. Call initOrThrow() at startup.");
        }
    }

     public static Jedis getResource() {
        ensureInit();
        return pool.getResource();
    }

    public static void close() {
        var p = pool;
        pool = null;
        initialized = false;
        if (p != null) {
            p.close();
            log.info("Redis pool closed");
        }
    }

    public void pingOrThrow() {
        ensureInit();
        try (Jedis jedis = RedisManager.getResource()) {
            String pong = jedis.ping();
            if (!"PONG".equalsIgnoreCase(pong)) {
                log.error("Redis ping failed: unexpected response='{}'", pong);
                throw new IllegalStateException("Redis ping failed: " + pong);
            }
            log.info("Redis ping OK: '{}'", pong);

        } catch (Exception e) {
            log.error("Redis is not available: host={}, port={}", host, port, e);
            throw new RuntimeException("Redis is not available", e);
        }
    }

    public static int getCharmQueueTtlSec() { ensureInit(); return charmQueueTtlSec; }
    public static int getCharmEmptyTtlSec() { ensureInit(); return charmEmptyTtlSec; }
    public static int getCharmLockTtlSec()  { ensureInit(); return charmLockTtlSec; }
}