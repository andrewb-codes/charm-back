package ru.andrewb.charm.back.bootstrap;

import ru.andrewb.charm.back.config.Config;
import ru.andrewb.charm.back.dao.ProfileDao;
import ru.andrewb.charm.back.dao.ProfileLikeDao;
import ru.andrewb.charm.back.mapper.ProfileGetDtoToPdfMapper;
import ru.andrewb.charm.back.mapper.ProfileToProfileGetDtoMapper;
import ru.andrewb.charm.back.mapper.ProfileToUserDetailsDtoMapper;
import ru.andrewb.charm.back.mapper.ProfileUpdateDtoToProfileMapper;
import ru.andrewb.charm.back.service.CharmService;
import ru.andrewb.charm.back.service.ContentService;
import ru.andrewb.charm.back.service.ProfileCacheService;
import ru.andrewb.charm.back.service.ProfileService;

import java.nio.file.Path;

public final class AppComponents {

    public static final ContentService CONTENT_SERVICE = new ContentService(
            Path.of(Config.required("app.content.base-path"))
    );

    public static final ProfileGetDtoToPdfMapper PROFILE_GET_DTO_TO_PDF_MAPPER =
            new ProfileGetDtoToPdfMapper(CONTENT_SERVICE);

    public static final ProfileService PROFILE_SERVICE = new ProfileService(
            ProfileDao.getInstance(),
            CONTENT_SERVICE,
            ProfileToProfileGetDtoMapper.getInstance(),
            ProfileUpdateDtoToProfileMapper.getInstance(),
            ProfileToUserDetailsDtoMapper.getInstance()
    );

    public static final CharmService CHARM_SERVICE = new CharmService(
            ProfileDao.getInstance(),
            ProfileLikeDao.getInstance(),
            ProfileCacheService.getInstance()
    );

    private AppComponents() {
    }
}
