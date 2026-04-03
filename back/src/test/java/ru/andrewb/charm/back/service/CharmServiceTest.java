package ru.andrewb.charm.back.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.andrewb.charm.back.dao.ProfileDao;
import ru.andrewb.charm.back.dao.ProfileLikeDao;
import ru.andrewb.charm.back.dto.Action;
import ru.andrewb.charm.back.dto.CharmDto;
import ru.andrewb.charm.back.dto.ProfileSimpleDto;

import java.util.ArrayDeque;
import java.util.Optional;
import java.util.Queue;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CharmServiceTest {

    @Mock
    private ProfileDao profileDao;

    @Mock
    private ProfileLikeDao profileLikeDao;

    @Mock
    private ProfileCacheService profileCacheService;

    private CharmService service;

    @BeforeEach
    void setUp() {
        service = new CharmService(
                profileDao,
                profileLikeDao,
                profileCacheService
        );
    }

    @Test
    void getNext_shouldReturnCandidateFromCache_whenCacheHasValue() {
        CharmDto dto = buildDto(1L, 2L, Action.SKIP);
        ProfileSimpleDto cached = candidate(100L, "Cached");

        when(profileCacheService.pollNext(1L)).thenReturn(cached);

        Optional<ProfileSimpleDto> result = service.getNext(dto);

        assertTrue(result.isPresent());
        assertSame(cached, result.get());

        verify(profileCacheService).pollNext(1L);
        verify(profileCacheService, never()).isEmptyCooldownActive(anyLong());
        verify(profileCacheService, never()).tryAcquireLock(anyLong());
        verify(profileDao, never()).findSuitableForUser(anyLong(), anyInt());
        verify(profileLikeDao, never()).likeOrDislike(anyLong(), anyLong(), anyBoolean());
    }

    @Test
    void getNext_shouldCallLikeOrDislike_beforeFetchingNext_whenActionIsLike() {
        CharmDto dto = buildDto(2L, 3L, Action.LIKE);
        ProfileSimpleDto cached = candidate(200L, "Cached");

        when(profileCacheService.pollNext(2L)).thenReturn(cached);

        Optional<ProfileSimpleDto> result = service.getNext(dto);

        assertTrue(result.isPresent());
        assertSame(cached, result.get());

        verify(profileLikeDao).likeOrDislike(2L, 3L, true);
        verify(profileCacheService).pollNext(2L);
    }

    @Test
    void getNext_shouldCallLikeOrDislikeWithFalse_whenActionIsDislike() {
        CharmDto dto = buildDto(3L, 4L, Action.DISLIKE);
        ProfileSimpleDto cached = candidate(300L, "Cached");

        when(profileCacheService.pollNext(3L)).thenReturn(cached);

        Optional<ProfileSimpleDto> result = service.getNext(dto);

        assertTrue(result.isPresent());
        assertSame(cached, result.get());

        verify(profileLikeDao).likeOrDislike(3L, 4L, false);
        verify(profileCacheService).pollNext(3L);
    }

    @Test
    void getNext_shouldNotCallLikeOrDislike_whenActionIsSkip() {
        CharmDto dto = buildDto(1L, 2L, Action.SKIP);

        when(profileCacheService.pollNext(1L)).thenReturn(null);
        when(profileCacheService.isEmptyCooldownActive(1L)).thenReturn(true);

        Optional<ProfileSimpleDto> result = service.getNext(dto);

        assertTrue(result.isEmpty());

        verify(profileLikeDao, never()).likeOrDislike(anyLong(), anyLong(), anyBoolean());
    }

    @Test
    void getNext_shouldReturnEmpty_whenEmptyCooldownIsActive() {
        CharmDto dto = buildDto(1L, null, Action.SKIP);

        when(profileCacheService.pollNext(1L)).thenReturn(null);
        when(profileCacheService.isEmptyCooldownActive(1L)).thenReturn(true);

        Optional<ProfileSimpleDto> result = service.getNext(dto);

        assertTrue(result.isEmpty());

        verify(profileCacheService).pollNext(1L);
        verify(profileCacheService).isEmptyCooldownActive(1L);
        verify(profileCacheService, never()).tryAcquireLock(anyLong());
        verify(profileDao, never()).findSuitableForUser(anyLong(), anyInt());
    }

    @Test
    void getNext_shouldReturnEmpty_whenLockIsNotAcquired() {
        CharmDto dto = buildDto(1L, null, Action.SKIP);

        when(profileCacheService.pollNext(1L)).thenReturn(null);
        when(profileCacheService.isEmptyCooldownActive(1L)).thenReturn(false);
        when(profileCacheService.tryAcquireLock(1L)).thenReturn(null);

        Optional<ProfileSimpleDto> result = service.getNext(dto);

        assertTrue(result.isEmpty());

        verify(profileCacheService).pollNext(1L);
        verify(profileCacheService).isEmptyCooldownActive(1L);
        verify(profileCacheService).tryAcquireLock(1L);
        verify(profileDao, never()).findSuitableForUser(anyLong(), anyInt());
        verify(profileCacheService, never()).releaseLock(anyLong(), any());
    }

    @Test
    void getNext_shouldRefillFromDaoAndCacheRest_whenLockIsAcquired() {
        CharmDto dto = buildDto(1L, null, Action.SKIP);

        ProfileSimpleDto first = candidate(10L, "First");
        ProfileSimpleDto second = candidate(11L, "Second");
        Queue<ProfileSimpleDto> queue = new ArrayDeque<>();
        queue.add(first);
        queue.add(second);

        when(profileCacheService.pollNext(1L)).thenReturn(null);
        when(profileCacheService.isEmptyCooldownActive(1L)).thenReturn(false);
        when(profileCacheService.tryAcquireLock(1L)).thenReturn("token-1");
        when(profileDao.findSuitableForUser(1L, 5)).thenReturn(queue);

        Optional<ProfileSimpleDto> result = service.getNext(dto);

        assertTrue(result.isPresent());
        assertSame(first, result.get());

        verify(profileDao).findSuitableForUser(1L, 5);
        verify(profileCacheService).clearEmptyCooldown(1L);
        verify(profileCacheService).replaceQueue(eq(1L), any(Queue.class));
        verify(profileCacheService).releaseLock(1L, "token-1");
        verify(profileCacheService, never()).markEmptyCooldown(anyLong());
    }

    @Test
    void getNext_shouldReturnEmptyAndMarkCooldown_whenDaoReturnsNoCandidates() {
        CharmDto dto = buildDto(1L, null, Action.SKIP);

        Queue<ProfileSimpleDto> queue = new ArrayDeque<>();

        when(profileCacheService.pollNext(1L)).thenReturn(null);
        when(profileCacheService.isEmptyCooldownActive(1L)).thenReturn(false);
        when(profileCacheService.tryAcquireLock(1L)).thenReturn("token-1");
        when(profileDao.findSuitableForUser(1L, 5)).thenReturn(queue);

        Optional<ProfileSimpleDto> result = service.getNext(dto);

        assertTrue(result.isEmpty());

        verify(profileDao).findSuitableForUser(1L, 5);
        verify(profileCacheService).markEmptyCooldown(1L);
        verify(profileCacheService, never()).clearEmptyCooldown(anyLong());
        verify(profileCacheService, never()).replaceQueue(anyLong(), any());
        verify(profileCacheService).releaseLock(1L, "token-1");
    }

    private CharmDto buildDto(Long fromId, Long toId, Action action) {
        CharmDto dto = new CharmDto();
        dto.setFromProfileId(fromId);
        dto.setToProfileId(toId);
        dto.setAction(action);
        return dto;
    }

    private ProfileSimpleDto candidate(Long id, String name) {
        ProfileSimpleDto dto = new ProfileSimpleDto();
        dto.setId(id);
        dto.setName(name);
        return dto;
    }
}
