package ru.andrewb.charm.back.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import ru.andrewb.charm.back.config.AppRedisProperties;
import ru.andrewb.charm.back.dto.ProfileSimpleDto;
import ru.andrewb.charm.back.model.exception.InfrastructureException;

import java.time.Duration;
import java.util.List;
import java.util.Queue;
import java.util.UUID;

@Slf4j
@Service
public class ProfileCacheService {

    private static final String QUEUE_KEY_PREFIX = "charm:queue:";
    private static final String EMPTY_KEY_PREFIX = "charm:empty:";
    private static final String LOCK_KEY_PREFIX = "charm:lock:";
    private static final DefaultRedisScript<Long> RELEASE_LOCK_SCRIPT = new DefaultRedisScript<>(
            """
                    if redis.call('get', KEYS[1]) == ARGV[1] then
                        return redis.call('del', KEYS[1])
                    else
                        return 0
                    end
                    """,
            Long.class
    );

    private final StringRedisTemplate redisTemplate;
    private final AppRedisProperties properties;
    private final ObjectMapper objectMapper;

    public ProfileCacheService(
            StringRedisTemplate redisTemplate,
            AppRedisProperties properties,
            ObjectMapper objectMapper
    ) {
        this.redisTemplate = redisTemplate;
        this.properties = properties;
        this.objectMapper = objectMapper;
    }

    // --------------- lock ---------------
    public String tryAcquireLock(Long userId) {
        String lockKey = LOCK_KEY_PREFIX + userId;
        String token = UUID.randomUUID().toString();

        try {
            Boolean acquired = redisTemplate.opsForValue().setIfAbsent(
                    lockKey,
                    token,
                    Duration.ofSeconds(properties.getCharmLockTtlSec())
            );
            if (!Boolean.TRUE.equals(acquired)) {
                log.debug("Redis lock busy key={}", lockKey);
            }
            return Boolean.TRUE.equals(acquired) ? token : null;
        } catch (Exception e) {
            log.error("Redis lock acquire failed key={}", lockKey, e);
            throw new InfrastructureException("error.internal", e);
        }
    }

    public boolean releaseLock(Long userId, String token) {
        if (token == null) return false;

        String lockKey = LOCK_KEY_PREFIX + userId;

        try {
            Long res = redisTemplate.execute(RELEASE_LOCK_SCRIPT, List.of(lockKey), token);
            boolean released = Long.valueOf(1L).equals(res);
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

        try {
            String json = redisTemplate.opsForList().leftPop(key);
            if (json == null) return null;
            return objectMapper.readValue(json, ProfileSimpleDto.class);
        } catch (Exception e) {
            log.error("Redis queue poll failed key={}", key, e);
            throw new InfrastructureException("error.internal", e);
        }
    }

    public void replaceQueue(Long userId, Queue<ProfileSimpleDto> queue) {
        String key = QUEUE_KEY_PREFIX + userId;

        try {
            redisTemplate.delete(key);

            if (!queue.isEmpty()) {
                List<String> payload = queue.stream()
                        .map(dto -> {
                            try {
                                return objectMapper.writeValueAsString(dto);
                            } catch (Exception e) {
                                throw new InfrastructureException("error.internal", e);
                            }
                        })
                        .toList();
                redisTemplate.opsForList().rightPushAll(key, payload);
                redisTemplate.expire(key, Duration.ofSeconds(properties.getCharmQueueTtlSec()));
            }
        } catch (Exception e) {
            log.error("Redis queue replace failed key={} size={}", key, queue.size(), e);
            throw new InfrastructureException("error.internal", e);
        }
    }

    // --------------- empty-cooldown ---------------
    public boolean isEmptyCooldownActive(Long userId) {
        String key = EMPTY_KEY_PREFIX + userId;

        try {
            return redisTemplate.hasKey(key);
        } catch (Exception e) {
            log.error("Redis empty cooldown exists check failed key={}", key, e);
            throw new InfrastructureException("error.internal", e);
        }
    }

    public void markEmptyCooldown(Long userId) {
        String key = EMPTY_KEY_PREFIX + userId;

        try {
            redisTemplate.opsForValue().set(
                    key,
                    "1",
                    Duration.ofSeconds(properties.getCharmEmptyTtlSec())
            );
            log.debug("Redis empty cooldown set key={} ttl={}", key, properties.getCharmEmptyTtlSec());
        } catch (Exception e) {
            log.error("Redis empty cooldown set failed key={}", key, e);
            throw new InfrastructureException("error.internal", e);
        }
    }

    public void clearEmptyCooldown(Long userId) {
        String key = EMPTY_KEY_PREFIX + userId;

        try {
            redisTemplate.delete(key);
        } catch (Exception e) {
            log.error("Redis empty cooldown clear failed key={}", key, e);
            throw new InfrastructureException("error.internal", e);
        }
    }
}
