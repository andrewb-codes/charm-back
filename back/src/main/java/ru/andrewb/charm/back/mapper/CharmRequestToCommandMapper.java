package ru.andrewb.charm.back.mapper;

import org.springframework.stereotype.Component;
import ru.andrewb.charm.back.controller.request.CharmRequest;
import ru.andrewb.charm.back.service.command.CharmCommand;

@Component
public class CharmRequestToCommandMapper implements Mapper<CharmRequest, CharmCommand> {

    @Override
    public CharmCommand map(CharmRequest request) {
        return map(request, new CharmCommand());
    }

    @Override
    public CharmCommand map(CharmRequest request, CharmCommand dto) {
        dto.setAction(request.getAction());
        dto.setToProfileId(request.getToProfileId());
        return dto;
    }
}
