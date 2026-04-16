package ru.andrewb.charm.back.mapper;

import org.springframework.stereotype.Component;
import ru.andrewb.charm.back.controller.request.PasswordChangeRequest;
import ru.andrewb.charm.back.service.command.PasswordChangeCommand;

@Component
public class PasswordChangeRequestToCommandMapper implements Mapper<PasswordChangeRequest, PasswordChangeCommand> {

    @Override
    public PasswordChangeCommand map(PasswordChangeRequest request) {
        return map(request, new PasswordChangeCommand());
    }

    @Override
    public PasswordChangeCommand map(PasswordChangeRequest request, PasswordChangeCommand dto) {
        dto.setCurrentPassword(request.getCurrentPassword());
        dto.setNewPassword(request.getNewPassword());
        dto.setConfirmPassword(request.getConfirmPassword());
        dto.setVersion(request.getVersion());
        return dto;
    }
}
