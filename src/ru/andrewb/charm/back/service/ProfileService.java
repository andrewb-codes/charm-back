package ru.andrewb.charm.back.service;

import ru.andrewb.charm.back.dao.ProfileDao;
import ru.andrewb.charm.back.dto.ProfileGetDto;
import ru.andrewb.charm.back.mapper.ProfileGetDtoMapper;
import ru.andrewb.charm.back.model.Profile;

import java.util.List;
import java.util.Optional;

public class ProfileService {

    private static final ProfileService INSTANCE = new ProfileService();

    private final ProfileDao dao = ProfileDao.getInstance();

    private final ProfileGetDtoMapper profileGetDtoMapper = ProfileGetDtoMapper.getInstance();

    private ProfileService() {
    }

    public static ProfileService getInstance() {
        return INSTANCE;
    }

    public Long save(Profile profile) {
        return dao.save(profile).getId();
    }

    public Optional<ProfileGetDto> findById(Long id) {
        if (id == null) return Optional.empty();
        return dao.findById(id).map(profileGetDtoMapper::map);
    }

    public List<ProfileGetDto> findAll() {
        return dao.findAll().stream().map(profileGetDtoMapper::map).toList();
    }

    public void update(Profile profile) {
        dao.update(profile);
    }

    public boolean delete(Long id) {
        if (id == null) return false;
        return dao.delete(id);
    }
}
