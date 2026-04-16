package ru.andrewb.charm.back.mapper;

import org.springframework.stereotype.Component;
import ru.andrewb.charm.back.controller.request.ProfileUpdateRequest;
import ru.andrewb.charm.back.dto.ProfileGetDto;

@Component
public class ProfileGetDtoToProfileUpdateRequestMapper implements Mapper<ProfileGetDto, ProfileUpdateRequest> {

    @Override
    public ProfileUpdateRequest map(ProfileGetDto dto) {
        return map(dto, new ProfileUpdateRequest());
    }

    @Override
    public ProfileUpdateRequest map(ProfileGetDto dto, ProfileUpdateRequest request) {
        request.setName(dto.getName());
        request.setSurname(dto.getSurname());
        request.setAbout(dto.getAbout());
        request.setBirthdate(dto.getBirthdate());
        request.setGender(dto.getGender());
        request.setVersion(dto.getVersion());
        return request;
    }
}
