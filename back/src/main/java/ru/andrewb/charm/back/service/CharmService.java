package ru.andrewb.charm.back.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.andrewb.charm.back.dao.ProfileDao;
import ru.andrewb.charm.back.dao.ProfileLikeDao;
import ru.andrewb.charm.back.dto.Action;
import ru.andrewb.charm.back.dto.ProfileSimpleDto;
import ru.andrewb.charm.back.service.command.CharmCommand;

import java.util.Optional;
import java.util.Queue;

@Slf4j
@Service
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

    public Optional<ProfileSimpleDto> getNext(CharmCommand command) {
        Long fromId = command.getFromProfileId();
        Long toId = command.getToProfileId();
        Action action = command.getAction();
        log.debug("Charm getNext userId={} action={} targetId={}", fromId, action, toId);

        if (action != Action.SKIP && toId != null) {
            boolean isLike = action == Action.LIKE;
            profileLikeDao.likeOrDislike(fromId, toId, isLike);
            log.debug("Charm action saved userId={} targetId={} like={}", fromId, toId, isLike);
        }

        // Try redis queue
        ProfileSimpleDto next = profileCacheService.pollNext(fromId);
        if (next != null) {
            log.debug("Charm cache hit userId={} nextId={}", fromId, next.getId());
            return Optional.of(next);
        }

        // If "empty marker" active -> don't hit DB
        if (profileCacheService.isEmptyCooldownActive(fromId)) {
            log.debug("Charm cooldown active userId={}", fromId);
            return Optional.empty();
        }

        // Try to acquire per-user refill lock
        String token = profileCacheService.tryAcquireLock(fromId);
        if (token == null) {
            // Someone already refills the queue now -> don't spam redis/db
            log.debug("Charm refill lock busy userId={}", fromId);
            return Optional.empty();
        }

        // Lock is free
        try {
            // Hit DB once per cooldown window
            Queue<ProfileSimpleDto> queue = profileDao.findSuitableForUser(fromId, 5);
            next = queue.poll();

            if (next == null) {  // no one candidate
                profileCacheService.markEmptyCooldown(fromId);
                log.info("Charm no candidates userId={}, empty cooldown set", fromId);
                return Optional.empty();
            }

            // at least 1 candidate
            profileCacheService.clearEmptyCooldown(fromId);
            if (!queue.isEmpty()) profileCacheService.replaceQueue(fromId, queue);
            log.debug("Charm db refill userId={} nextId={} cachedRest={}", fromId, next.getId(), queue.size());

            return Optional.of(next);

        } finally {
            // Release lock safely
            profileCacheService.releaseLock(fromId, token);
        }
    }
}
