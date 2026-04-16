package ru.andrewb.charm.back.mapper;

import org.springframework.stereotype.Component;
import ru.andrewb.charm.back.model.Profile;
import ru.andrewb.charm.back.service.command.ProfileUpdateCommand;

@Component
public class ProfileUpdateCommandToProfileMapper implements Mapper<ProfileUpdateCommand, Profile> {

    @Override
    public Profile map(ProfileUpdateCommand command) {
        return map(command, new Profile());
    }

    @Override
    public Profile map(ProfileUpdateCommand command, Profile profile) {
        if (command.getVersion() != null) profile.setVersion(command.getVersion());
        if (command.getName() != null) profile.setName(command.getName());
        if (command.getSurname() != null) profile.setSurname(command.getSurname());
        if (command.getBirthdate() != null) profile.setBirthdate(command.getBirthdate());
        if (command.getAbout() != null) profile.setAbout(command.getAbout());
        if (command.getGender() != null) profile.setGender(command.getGender());
        return profile;
    }
}
