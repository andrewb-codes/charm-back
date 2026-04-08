package ru.andrewb.charm.back.service;

import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.params.SetParams;
import ru.andrewb.charm.back.config.AppRedisProperties;
import ru.andrewb.charm.back.dto.ProfileSimpleDto;
import ru.andrewb.charm.back.mapper.JsonMapper;

import java.util.Queue;
import java.util.UUID;

@Service
public class ProfileCacheService {

    private static final String QUEUE_KEY_PREFIX = "charm:queue:";
    private static final String EMPTY_KEY_PREFIX = "charm:empty:";
    private static final String LOCK_KEY_PREFIX = "charm:lock:";

    private final JedisPool jedisPool;
    private final AppRedisProperties properties;
    private final JsonMapper jsonMapper = JsonMapper.getInstance();

    public ProfileCacheService(JedisPool jedisPool, AppRedisProperties properties) {
        this.jedisPool = jedisPool;
        this.properties = properties;
    }

    // --------------- lock ---------------
    public String tryAcquireLock(Long userId) {
        String lockKey = LOCK_KEY_PREFIX + userId;
        String token = UUID.randomUUID().toString();

        try (Jedis jedis = jedisPool.getResource()) {
            String res = jedis.set(lockKey, token, SetParams.setParams().nx().ex(properties.getCharmLockTtlSec()));
            return "OK".equals(res) ? token : null;
        } catch (Exception e) {
            throw new RuntimeException("redis tryAcquireLock failed", e);
        }
    }

    public boolean releaseLock(Long userId, String token) {
        if (token == null) return false;

        String lockKey = LOCK_KEY_PREFIX + userId;

        String lua = """
                if redis.call('get', KEYS[1]) == ARGV[1] then
                    return redis.call('del', KEYS[1])
                else
                    return 0
                end
                """;

        try (Jedis jedis = jedisPool.getResource()) {
            Object res = jedis.eval(lua, 1, lockKey, token);
            return res.equals(1L);
        } catch (Exception e) {
            throw new RuntimeException("redis releaseLock failed", e);
        }
    }

    // --------------- queue ---------------
    public ProfileSimpleDto pollNext(Long userId) {
        String key = QUEUE_KEY_PREFIX + userId;

        try (Jedis jedis = jedisPool.getResource()) {
            String json = jedis.lpop(key);
            if (json == null) return null;
            return jsonMapper.readValue(json, ProfileSimpleDto.class);
        } catch (Exception e) {
            throw new RuntimeException("redis poll failed", e);
        }
    }

    public void replaceQueue(Long userId, Queue<ProfileSimpleDto> queue) {
        String key = QUEUE_KEY_PREFIX + userId;

        try (Jedis jedis = jedisPool.getResource()) {
            var p = jedis.pipelined();
            p.del(key);

            for (ProfileSimpleDto dto : queue) {
                String json = jsonMapper.writeValueAsString(dto);
                p.rpush(key, json);
            }

            p.expire(key, properties.getCharmQueueTtlSec());
            p.sync();
        } catch (Exception e) {
            throw new RuntimeException("redis replaceQueue failed", e);
        }
    }

    // --------------- empty-cooldown ---------------
    public boolean isEmptyCooldownActive(Long userId) {
        String key = EMPTY_KEY_PREFIX + userId;

        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.exists(key);
        }
    }

    public void markEmptyCooldown(Long userId) {
        String key = EMPTY_KEY_PREFIX + userId;

        try (Jedis jedis = jedisPool.getResource()) {
            jedis.setex(key, properties.getCharmEmptyTtlSec(), "1");
        }
    }

    public void clearEmptyCooldown(Long userId) {
        String key = EMPTY_KEY_PREFIX + userId;

        try (Jedis jedis = jedisPool.getResource()) {
            jedis.del(key);
        }
    }
}