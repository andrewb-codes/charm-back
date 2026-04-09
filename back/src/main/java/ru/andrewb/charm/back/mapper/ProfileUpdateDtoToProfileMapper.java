package ru.andrewb.charm.back.mapper;

import org.springframework.stereotype.Component;
import ru.andrewb.charm.back.dto.ProfileUpdateDto;
import ru.andrewb.charm.back.model.Profile;

@Component
public class ProfileUpdateDtoToProfileMapper implements Mapper<ProfileUpdateDto, Profile> {

    @Override
    public Profile map(ProfileUpdateDto dto) {
        return map(dto, new Profile());
    }

    @Override
    public Profile map(ProfileUpdateDto dto, Profile profile) {
        if (dto.getVersion() != null) profile.setVersion(dto.getVersion());
        if (dto.getName() != null) profile.setName(dto.getName());
        if (dto.getSurname() != null) profile.setSurname(dto.getSurname());
        if (dto.getBirthdate() != null) profile.setBirthdate(dto.getBirthdate());
        if (dto.getAbout() != null) profile.setAbout(dto.getAbout());
        if (dto.getGender() != null) profile.setGender(dto.getGender());
        return profile;
    }
}
