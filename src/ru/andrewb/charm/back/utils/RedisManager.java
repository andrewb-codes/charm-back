package ru.andrewb.charm.back.utils;

import lombok.experimental.UtilityClass;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

@UtilityClass
public class RedisManager {

    private static final String HOST = Config.required("app.redis.host");
    private static final int PORT = Integer.parseInt(Config.getOrDefault("app.redis.port", "6379"));

    public static final int CHARM_QUEUE_TTL_SEC =
            Integer.parseInt(Config.getOrDefault("app.redis.charm-queue-ttl-sec", "300"));
    public static final int CHARM_EMPTY_TTL_SEC =
            Integer.parseInt(Config.getOrDefault("app.redis.charm-empty-ttl-sec", "60"));

    // pool tuning
    private static final int TIMEOUT_MS = Integer.parseInt(Config.getOrDefault("app.redis.timeout-ms", "2000"));
    private static final int MAX_TOTAL = Integer.parseInt(Config.getOrDefault("app.redis.pool.max-total", "16"));
    private static final int MAX_IDLE = Integer.parseInt(Config.getOrDefault("app.redis.pool.max-idle", "16"));
    private static final int MIN_IDLE = Integer.parseInt(Config.getOrDefault("app.redis.pool.min-idle", "0"));
    private static final boolean TEST_ON_BORROW =
            Boolean.parseBoolean(Config.getOrDefault("app.redis.pool.test-on-borrow", "true"));

    private static JedisPool pool;

    static {
        init();
    }

    private static void init() {
        JedisPoolConfig cfg = new JedisPoolConfig();
        cfg.setMaxTotal(MAX_TOTAL);
        cfg.setMaxIdle(MAX_IDLE);
        cfg.setMinIdle(MIN_IDLE);
        cfg.setTestOnBorrow(TEST_ON_BORROW);

        pool = new JedisPool(cfg, HOST, PORT, TIMEOUT_MS);
    }

     public static Jedis getResource() {
        return pool.getResource();
    }

    public static void close() {
        var p = pool;
        if (p != null) p.close();
    }
}