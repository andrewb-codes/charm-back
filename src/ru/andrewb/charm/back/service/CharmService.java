package ru.andrewb.charm.back.service;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.andrewb.charm.back.dao.ProfileDao;
import ru.andrewb.charm.back.dao.ProfileLikeDao;
import ru.andrewb.charm.back.dto.Action;
import ru.andrewb.charm.back.dto.CharmDto;
import ru.andrewb.charm.back.dto.ProfileSimpleDto;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CharmService {

    private static final CharmService INSTANCE = new CharmService();

    private final ProfileDao profileDao = ProfileDao.getInstance();
    private final ProfileLikeDao profileLikeDao = ProfileLikeDao.getInstance();
    private final Map<Long, Queue<ProfileSimpleDto>> cache = new ConcurrentHashMap<>();

    public static CharmService getInstance() {
        return INSTANCE;
    }

    public Optional<ProfileSimpleDto> getNext(CharmDto dto) {
        Long fromId = dto.getFromProfileId();
        Long toId = dto.getToProfileId();
        Action action = dto.getAction();
        boolean isLike = action == Action.LIKE;

        if (action != Action.SKIP && toId != null) {
            profileLikeDao.likeOrDislike(fromId, toId, isLike);
        }

        AtomicReference<ProfileSimpleDto> out = new AtomicReference<>();
        cache.compute(fromId, (k, q) -> {
            if (q == null || q.isEmpty()) q = profileDao.findSuitableForUser(fromId, 5);
            out.set(q.poll());
            return q;
        });

        return Optional.ofNullable(out.get());
    }
}