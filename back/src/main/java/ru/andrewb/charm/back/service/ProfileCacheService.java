package ru.andrewb.charm.back.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.params.SetParams;
import ru.andrewb.charm.back.config.AppRedisProperties;
import ru.andrewb.charm.back.dto.ProfileSimpleDto;
import ru.andrewb.charm.back.model.exception.InfrastructureException;

import java.util.Queue;
import java.util.UUID;

@Slf4j
@Service
public class ProfileCacheService {

    private static final String QUEUE_KEY_PREFIX = "charm:queue:";
    private static final String EMPTY_KEY_PREFIX = "charm:empty:";
    private static final String LOCK_KEY_PREFIX = "charm:lock:";

    private final JedisPool jedisPool;
    private final AppRedisProperties properties;
    private final ObjectMapper objectMapper;

    public ProfileCacheService(
            JedisPool jedisPool,
            AppRedisProperties properties,
            ObjectMapper objectMapper
    ) {
        this.jedisPool = jedisPool;
        this.properties = properties;
        this.objectMapper = objectMapper;
    }

    // --------------- lock ---------------
    public String tryAcquireLock(Long userId) {
        String lockKey = LOCK_KEY_PREFIX + userId;
        String token = UUID.randomUUID().toString();

        try (Jedis jedis = jedisPool.getResource()) {
            String res = jedis.set(lockKey, token, SetParams.setParams().nx().ex(properties.getCharmLockTtlSec()));
            if (!"OK".equals(res)) {
                log.debug("Redis lock busy key={}", lockKey);
            }
            return "OK".equals(res) ? token : null;
        } catch (Exception e) {
            log.error("Redis lock acquire failed key={}", lockKey, e);
            throw new InfrastructureException("error.internal", e);
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
            boolean released = res.equals(1L);
            if (!released) {
                log.debug("Redis lock release skipped key={} tokenMismatchOrExpired=true", lockKey);
            }
            return released;
        } catch (Exception e) {
            log.error("Redis lock release failed key={}", lockKey, e);
            throw new InfrastructureException("error.internal", e);
        }
    }

    // --------------- queue ---------------
    public ProfileSimpleDto pollNext(Long userId) {
        String key = QUEUE_KEY_PREFIX + userId;

        try (Jedis jedis = jedisPool.getResource()) {
            String json = jedis.lpop(key);
            if (json == null) return null;
            return objectMapper.readValue(json, ProfileSimpleDto.class);
        } catch (Exception e) {
            log.error("Redis queue poll failed key={}", key, e);
            throw new InfrastructureException("error.internal", e);
        }
    }

    public void replaceQueue(Long userId, Queue<ProfileSimpleDto> queue) {
        String key = QUEUE_KEY_PREFIX + userId;

        try (Jedis jedis = jedisPool.getResource()) {
            var p = jedis.pipelined();
            p.del(key);

            for (ProfileSimpleDto dto : queue) {
                String json = objectMapper.writeValueAsString(dto);
                p.rpush(key, json);
            }

            p.expire(key, properties.getCharmQueueTtlSec());
            p.sync();
        } catch (Exception e) {
            log.error("Redis queue replace failed key={} size={}", key, queue.size(), e);
            throw new InfrastructureException("error.internal", e);
        }
    }

    // --------------- empty-cooldown ---------------
    public boolean isEmptyCooldownActive(Long userId) {
        String key = EMPTY_KEY_PREFIX + userId;

        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.exists(key);
        } catch (Exception e) {
            log.error("Redis empty cooldown exists check failed key={}", key, e);
            throw new InfrastructureException("error.internal", e);
        }
    }

    public void markEmptyCooldown(Long userId) {
        String key = EMPTY_KEY_PREFIX + userId;

        try (Jedis jedis = jedisPool.getResource()) {
            jedis.setex(key, properties.getCharmEmptyTtlSec(), "1");
            log.debug("Redis empty cooldown set key={} ttl={}", key, properties.getCharmEmptyTtlSec());
        } catch (Exception e) {
            log.error("Redis empty cooldown set failed key={}", key, e);
            throw new InfrastructureException("error.internal", e);
        }
    }

    public void clearEmptyCooldown(Long userId) {
        String key = EMPTY_KEY_PREFIX + userId;

        try (Jedis jedis = jedisPool.getResource()) {
            jedis.del(key);
        } catch (Exception e) {
            log.error("Redis empty cooldown clear failed key={}", key, e);
            throw new InfrastructureException("error.internal", e);
        }
    }
}
