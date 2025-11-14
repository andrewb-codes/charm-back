package ru.andrewb.charm.back.service;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.andrewb.charm.back.dao.ProfileDao;
import ru.andrewb.charm.back.dao.ProfileLikeDao;
import ru.andrewb.charm.back.dto.Action;
import ru.andrewb.charm.back.dto.CharmDto;
import ru.andrewb.charm.back.dto.ProfileSimpleDto;

import java.util.List;
import java.util.Optional;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CharmService {

    private static final CharmService INSTANCE = new CharmService();

    private final ProfileDao profileDao = ProfileDao.getInstance();
    private final ProfileLikeDao profileLikeDao = ProfileLikeDao.getInstance();

    public static CharmService getInstance() {
        return INSTANCE;
    }

    public Optional<ProfileSimpleDto> getNext(CharmDto dto) {
        Long fromId = dto.getFromProfileId();
        Long toId = dto.getToProfileId();
        Action action = dto.getAction();
        boolean isLike = action == Action.LIKE;

        if (dto.getAction() != Action.SKIP && dto.getToProfileId() != null) {
            profileLikeDao.likeOrDislike(fromId, toId, isLike);
        }

        List<ProfileSimpleDto> suitableProfiles = profileDao.findSuitableForUser(fromId, 1);

        return suitableProfiles.stream().findFirst();
    }
}