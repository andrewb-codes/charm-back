package ru.andrewb.charm.back.service;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.andrewb.charm.back.dao.ProfileDao;
import ru.andrewb.charm.back.dao.ProfileLikeDao;
import ru.andrewb.charm.back.dto.Action;
import ru.andrewb.charm.back.dto.CharmDto;
import ru.andrewb.charm.back.dto.ProfileSimpleDto;

import java.util.Optional;
import java.util.Queue;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CharmService {

    private static final CharmService INSTANCE = new CharmService();

    private final ProfileDao profileDao = ProfileDao.getInstance();
    private final ProfileLikeDao profileLikeDao = ProfileLikeDao.getInstance();
    private final ProfileCacheService profileCacheService = ProfileCacheService.getInstance();

    public static CharmService getInstance() {
        return INSTANCE;
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

        // Hit DB once per cooldown window
        Queue<ProfileSimpleDto> queue = profileDao.findSuitableForUser(fromId, 5);
        next = queue.poll();

        if (next == null) {  // no one
            profileCacheService.markEmptyCooldown(fromId);
            return Optional.empty();
        }

        // at least 1 candidate
        profileCacheService.clearEmptyCooldown(fromId);
        if (!queue.isEmpty()) profileCacheService.replaceQueue(fromId, queue);

        return Optional.of(next);
    }
}