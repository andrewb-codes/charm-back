package ru.andrewb.charm.back.mapper;

import ru.andrewb.charm.back.dto.ProfileGetDto;
import ru.andrewb.charm.back.model.Profile;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class ProfileGetDtoMapper implements Mapper<Profile, ProfileGetDto> {

    private static final ProfileGetDtoMapper INSTANCE = new ProfileGetDtoMapper();

    private ProfileGetDtoMapper() {
    }

    public static ProfileGetDtoMapper getInstance() {
        return INSTANCE;
    }

    @Override
    public ProfileGetDto map(Profile profile) {
        return map(profile, new ProfileGetDto());
    }

    @Override
    public ProfileGetDto map(Profile profile, ProfileGetDto dto) {
        dto.setId(profile.getId());
        dto.setEmail(profile.getEmail());
        dto.setName(profile.getName());
        dto.setSurname(profile.getSurname());
        dto.setAbout(profile.getAbout());
        dto.setBirthDate(profile.getBirthDate());
        if (profile.getBirthDate() != null) {
            dto.setAge(Math.toIntExact(ChronoUnit.YEARS.between(profile.getBirthDate(), LocalDate.now())));
        } else {
            dto.setAge(null);
        }
        dto.setGender(profile.getGender());
        return dto;
    }
}
