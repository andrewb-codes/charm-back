package ru.andrewb.charm.back.mapper;

import ru.andrewb.charm.back.dto.ProfileSaveDto;
import ru.andrewb.charm.back.model.Profile;

public class ProfileMapper implements Mapper<ProfileSaveDto, Profile> {

    private static final ProfileMapper INSTANCE = new ProfileMapper();

    private ProfileMapper() {}

    public static ProfileMapper getInstance() {
        return INSTANCE;
    }

    @Override
    public Profile map(ProfileSaveDto dto) {
        Profile profile = new Profile();
        profile.setEmail(dto.getEmail());
        profile.setName(dto.getName());
        profile.setSurname(dto.getSurname());
        profile.setAbout(dto.getAbout());
        profile.setBirthDate(dto.getBirthDate());
        profile.setGender(dto.getGender());

        return profile;
    }
}
