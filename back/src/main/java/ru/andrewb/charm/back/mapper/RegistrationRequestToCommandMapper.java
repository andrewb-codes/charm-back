package ru.andrewb.charm.back.mapper;

import org.springframework.stereotype.Component;
import ru.andrewb.charm.back.controller.request.RegistrationRequest;
import ru.andrewb.charm.back.service.command.RegistrationCommand;

@Component
public class RegistrationRequestToCommandMapper implements Mapper<RegistrationRequest, RegistrationCommand> {

    @Override
    public RegistrationCommand map(RegistrationRequest request) {
        return map(request, new RegistrationCommand());
    }

    @Override
    public RegistrationCommand map(RegistrationRequest request, RegistrationCommand dto) {
        dto.setEmail(request.getEmail());
        dto.setPassword(request.getPassword());
        return dto;
    }
}
