package ru.andrewb.charm.back.service;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import redis.clients.jedis.Jedis;
import ru.andrewb.charm.back.dto.ProfileSimpleDto;
import ru.andrewb.charm.back.mapper.JsonMapper;
import ru.andrewb.charm.back.utils.RedisManager;

import java.util.Queue;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ProfileCacheService {

    private static final ProfileCacheService INSTANCE = new ProfileCacheService();

    private static final String QUEUE_KEY_PREFIX = "charm:queue:";
    private static final String EMPTY_KEY_PREFIX = "charm:empty:";

    private final JsonMapper jsonMapper = JsonMapper.getInstance();

    public static ProfileCacheService getInstance() {
        return INSTANCE;
    }

    public ProfileSimpleDto pollNext(Long userId) {
        String key = QUEUE_KEY_PREFIX + userId;
        try (Jedis jedis = RedisManager.getResource()) {
            String json = jedis.lpop(key);
            if (json == null) return null;
            return jsonMapper.readValue(json, ProfileSimpleDto.class);
        } catch (Exception e) {
            throw new RuntimeException("redis poll failed", e);
        }
    }

    public void replaceQueue(Long userId, Queue<ProfileSimpleDto> queue) {
        String key = QUEUE_KEY_PREFIX + userId;
        try (Jedis jedis = RedisManager.getResource()) {
            var p = jedis.pipelined();
            p.del(key);

            for (ProfileSimpleDto dto : queue) {
                String json = jsonMapper.writeValueAsString(dto);
                p.rpush(key, json);
            }
            p.expire(key, RedisManager.CHARM_QUEUE_TTL_SEC);
            p.sync();
        } catch (Exception e) {
            throw new RuntimeException("redis replaceQueue failed", e);
        }
    }

    public boolean isEmptyCooldownActive(Long userId) {
        String key = EMPTY_KEY_PREFIX + userId;
        try (Jedis jedis = RedisManager.getResource()) {
            return jedis.exists(key);
        }
    }

    public void markEmptyCooldown(Long userId) {
        String key = EMPTY_KEY_PREFIX + userId;
        try (Jedis jedis = RedisManager.getResource()) {
            jedis.setex(key, RedisManager.CHARM_EMPTY_TTL_SEC, "1");
        }
    }

    public void clearEmptyCooldown(Long userId) {
        String key = EMPTY_KEY_PREFIX + userId;
        try (Jedis jedis = RedisManager.getResource()) {
            jedis.del(key);
        }
    }
}