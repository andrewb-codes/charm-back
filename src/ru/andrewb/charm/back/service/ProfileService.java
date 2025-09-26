package ru.andrewb.charm.back.service;

import ru.andrewb.charm.back.dao.ProfileDao;
import ru.andrewb.charm.back.dto.ProfileGetDto;
import ru.andrewb.charm.back.dto.ProfileSaveDto;
import ru.andrewb.charm.back.mapper.ProfileGetDtoMapper;
import ru.andrewb.charm.back.mapper.ProfileMapper;
import ru.andrewb.charm.back.model.Profile;

import java.util.List;
import java.util.Optional;

public class ProfileService {

    private static final ProfileService INSTANCE = new ProfileService();

    private final ProfileDao dao = ProfileDao.getInstance();

    private final ProfileGetDtoMapper profileGetDtoMapper = ProfileGetDtoMapper.getInstance();
    private final ProfileMapper profileMapper = ProfileMapper.getInstance();

    private ProfileService() {
    }

    public static ProfileService getInstance() {
        return INSTANCE;
    }

    public Long save(ProfileSaveDto dto) {
        Profile profile = profileMapper.map(dto);
        return dao.save(profile).getId();
    }

    public Optional<ProfileGetDto> findById(Long id) {
        if (id == null) return Optional.empty();
        return dao.findById(id).map(profileGetDtoMapper::map);
    }

    public List<ProfileGetDto> findAll() {
        return dao.findAll().stream().map(profileGetDtoMapper::map).toList();
    }

    public void update(Long id, ProfileSaveDto dto) {
        Profile profile = profileMapper.map(dto);
        profile.setId(id);
        dao.update(profile);
    }

    public boolean delete(Long id) {
        if (id == null) return false;
        return dao.delete(id);
    }
}
