package ru.andrewb.charm.back.mapper;

import org.springframework.stereotype.Component;
import ru.andrewb.charm.back.controller.request.ProfileUpdateRequest;
import ru.andrewb.charm.back.service.command.ProfileUpdateCommand;

@Component
public class ProfileUpdateRequestToCommandMapper implements Mapper<ProfileUpdateRequest, ProfileUpdateCommand> {

    @Override
    public ProfileUpdateCommand map(ProfileUpdateRequest request) {
        return map(request, new ProfileUpdateCommand());
    }

    @Override
    public ProfileUpdateCommand map(ProfileUpdateRequest request, ProfileUpdateCommand dto) {
        dto.setName(request.getName());
        dto.setSurname(request.getSurname());
        dto.setAbout(request.getAbout());
        dto.setBirthdate(request.getBirthdate());
        dto.setGender(request.getGender());
        dto.setVersion(request.getVersion());
        return dto;
    }
}
