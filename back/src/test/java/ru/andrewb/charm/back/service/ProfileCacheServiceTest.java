package ru.andrewb.charm.back.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.script.RedisScript;
import ru.andrewb.charm.back.config.AppRedisProperties;
import ru.andrewb.charm.back.dto.ProfileSimpleDto;
import ru.andrewb.charm.back.model.exception.InfrastructureException;

import java.util.ArrayDeque;
import java.util.List;
import java.util.Queue;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProfileCacheServiceTest {

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @Mock
    private ListOperations<String, String> listOperations;

    private ProfileCacheService service;

    @BeforeEach
    void setUp() {
        AppRedisProperties properties = new AppRedisProperties();
        properties.setCharmLockTtlSec(10);
        properties.setCharmQueueTtlSec(300);
        properties.setCharmEmptyTtlSec(60);
        service = new ProfileCacheService(redisTemplate, properties, new ObjectMapper());
    }

    @Test
    void tryAcquireLock_shouldReturnToken_whenRedisReturnsOk() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.setIfAbsent(eq("charm:lock:1"), any(), any())).thenReturn(true);

        String token = service.tryAcquireLock(1L);

        assertNotNull(token);
        assertFalse(token.isBlank());
    }

    @Test
    void tryAcquireLock_shouldReturnNull_whenRedisDoesNotReturnOk() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.setIfAbsent(eq("charm:lock:1"), any(), any())).thenReturn(false);

        String token = service.tryAcquireLock(1L);

        assertNull(token);
    }

    @Test
    void releaseLock_shouldReturnFalse_whenTokenIsNull() {
        assertFalse(service.releaseLock(1L, null));
        verifyNoInteractions(redisTemplate);
    }

    @Test
    void pollNext_shouldReturnDto_whenJsonExists() throws Exception {
        ProfileSimpleDto dto = new ProfileSimpleDto();
        dto.setId(15L);
        dto.setName("Alice");
        String json = new ObjectMapper().writeValueAsString(dto);

        when(redisTemplate.opsForList()).thenReturn(listOperations);
        when(listOperations.leftPop("charm:queue:5")).thenReturn(json);

        ProfileSimpleDto result = service.pollNext(5L);

        assertNotNull(result);
        assertEquals(15L, result.getId());
        assertEquals("Alice", result.getName());
    }

    @Test
    void pollNext_shouldThrowInfrastructureException_whenRedisFails() {
        when(redisTemplate.opsForList()).thenReturn(listOperations);
        when(listOperations.leftPop("charm:queue:5")).thenThrow(new RuntimeException("redis-down"));

        InfrastructureException ex = assertThrows(
                InfrastructureException.class,
                () -> service.pollNext(5L)
        );

        assertEquals("error.internal", ex.getMessage());
    }

    @Test
    void replaceQueue_shouldWriteAllItemsAndSetExpiry() {
        ProfileSimpleDto first = new ProfileSimpleDto();
        first.setId(1L);
        first.setName("A");
        ProfileSimpleDto second = new ProfileSimpleDto();
        second.setId(2L);
        second.setName("B");
        Queue<ProfileSimpleDto> queue = new ArrayDeque<>();
        queue.add(first);
        queue.add(second);

        when(redisTemplate.opsForList()).thenReturn(listOperations);

        service.replaceQueue(10L, queue);

        verify(redisTemplate).delete("charm:queue:10");
        verify(listOperations).rightPushAll(eq("charm:queue:10"), any(List.class));
        verify(redisTemplate).expire(eq("charm:queue:10"), any());
    }

    @Test
    void emptyCooldownMethods_shouldDelegateToRedis() {
        when(redisTemplate.hasKey("charm:empty:11")).thenReturn(true);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        assertTrue(service.isEmptyCooldownActive(11L));
        service.markEmptyCooldown(11L);
        service.clearEmptyCooldown(11L);

        verify(redisTemplate).hasKey("charm:empty:11");
        verify(valueOperations).set(eq("charm:empty:11"), eq("1"), any());
        verify(redisTemplate).delete("charm:empty:11");
    }

    @Test
    void releaseLock_shouldReturnTrue_whenScriptDeletesKey() {
        when(redisTemplate.execute(any(RedisScript.class), eq(List.of("charm:lock:1")), eq("token-1")))
                .thenReturn(1L);

        boolean released = service.releaseLock(1L, "token-1");

        assertTrue(released);
    }
}
