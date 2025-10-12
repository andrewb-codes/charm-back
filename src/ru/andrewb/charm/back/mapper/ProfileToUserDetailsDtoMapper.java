package ru.andrewb.charm.back.mapper;

import ru.andrewb.charm.back.dto.UserDetailsDto;
import ru.andrewb.charm.back.model.Profile;

public class ProfileToUserDetailsDtoMapper implements Mapper<Profile, UserDetailsDto> {

    private static final ProfileToUserDetailsDtoMapper INSTANCE = new ProfileToUserDetailsDtoMapper();

    public static ProfileToUserDetailsDtoMapper getInstance() {
        return INSTANCE;
    }

    @Override
    public UserDetailsDto map(Profile profile) {
        return map(profile, new UserDetailsDto());
    }

    @Override
    public UserDetailsDto map(Profile profile, UserDetailsDto dto) {
        dto.setId(profile.getId());
        dto.setEmail(profile.getEmail());
        dto.setRole(profile.getRole());
        return dto;
    }
}
