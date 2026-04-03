package ru.andrewb.charm.back.service;

import ru.andrewb.charm.back.dao.ProfileDao;
import ru.andrewb.charm.back.dao.ProfileLikeDao;
import ru.andrewb.charm.back.dto.Action;
import ru.andrewb.charm.back.dto.CharmDto;
import ru.andrewb.charm.back.dto.ProfileSimpleDto;

import java.util.Optional;
import java.util.Queue;

public class CharmService {

    private final ProfileDao profileDao;
    private final ProfileLikeDao profileLikeDao;
    private final ProfileCacheService profileCacheService;

    public CharmService(
            ProfileDao profileDao,
            ProfileLikeDao profileLikeDao,
            ProfileCacheService profileCacheService
    ) {
        this.profileDao = profileDao;
        this.profileLikeDao = profileLikeDao;
        this.profileCacheService = profileCacheService;
    }

    public Optional<ProfileSimpleDto> getNext(CharmDto dto) {
        Long fromId = dto.getFromProfileId();
        Long toId = dto.getToProfileId();
        Action action = dto.getAction();

        if (action != Action.SKIP && toId != null) {
            boolean isLike = action == Action.LIKE;
            profileLikeDao.likeOrDislike(fromId, toId, isLike);
        }

        // Try redis queue
        ProfileSimpleDto next = profileCacheService.pollNext(fromId);
        if (next != null) {
            return Optional.of(next);
        }

        // If "empty marker" active -> don't hit DB
        if (profileCacheService.isEmptyCooldownActive(fromId)) {
            return Optional.empty();
        }

        // Try to acquire per-user refill lock
        String token = profileCacheService.tryAcquireLock(fromId);
        if (token == null) {
            // Someone already refills the queue now -> don't spam redis/db
            return Optional.empty();
        }

        // Lock is free
        try {
            // Hit DB once per cooldown window
            Queue<ProfileSimpleDto> queue = profileDao.findSuitableForUser(fromId, 5);
            next = queue.poll();

            if (next == null) {  // no one candidate
                profileCacheService.markEmptyCooldown(fromId);
                return Optional.empty();
            }

            // at least 1 candidate
            profileCacheService.clearEmptyCooldown(fromId);
            if (!queue.isEmpty()) profileCacheService.replaceQueue(fromId, queue);

            return Optional.of(next);

        } finally {
            // Release lock safely
            profileCacheService.releaseLock(fromId, token);
        }
    }
}