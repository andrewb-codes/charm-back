package ru.andrewb.charm.back.mapper;

import org.springframework.stereotype.Component;
import ru.andrewb.charm.back.controller.request.EmailChangeRequest;
import ru.andrewb.charm.back.service.command.EmailChangeCommand;

@Component
public class EmailChangeRequestToCommandMapper implements Mapper<EmailChangeRequest, EmailChangeCommand> {

    @Override
    public EmailChangeCommand map(EmailChangeRequest request) {
        return map(request, new EmailChangeCommand());
    }

    @Override
    public EmailChangeCommand map(EmailChangeRequest request, EmailChangeCommand dto) {
        dto.setNewEmail(request.getNewEmail());
        dto.setCurrentPassword(request.getCurrentPassword());
        dto.setVersion(request.getVersion());
        return dto;
    }
}
