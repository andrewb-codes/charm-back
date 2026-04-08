package ru.andrewb.charm.back.mapper;

import org.springframework.stereotype.Component;
import ru.andrewb.charm.back.dto.UserDetailsDto;
import ru.andrewb.charm.back.model.Profile;

@Component
public class ProfileToUserDetailsDtoMapper implements Mapper<Profile, UserDetailsDto> {

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
