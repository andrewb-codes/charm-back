package ru.andrewb.charm.back.mapper;

import jakarta.servlet.http.HttpServletRequest;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.andrewb.charm.back.dto.Action;
import ru.andrewb.charm.back.dto.CharmDto;

import static ru.andrewb.charm.back.utils.StringUtils.stripToNull;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class RequestToCharmDtoMapper implements Mapper<HttpServletRequest, CharmDto> {

    private static final RequestToCharmDtoMapper INSTANCE = new RequestToCharmDtoMapper();

    public static RequestToCharmDtoMapper getInstance() {
        return INSTANCE;
    }

    @Override
    public CharmDto map(HttpServletRequest req) {
        return map(req, new CharmDto());
    }

    @Override
    public CharmDto map(HttpServletRequest req, CharmDto dto) {
        String actionStr = stripToNull(req.getParameter("action"));
        if (actionStr != null) {
            try {
                dto.setAction(Action.valueOf(actionStr.toUpperCase()));
            } catch (IllegalArgumentException ignored) {
                // action -> null
            }
        }

        String to = stripToNull(req.getParameter("toProfile"));
        if (to != null) {
            try {
                dto.setToProfileId(Long.parseLong(to));
            } catch (NumberFormatException ignored) {
                // to -> null
            }
        }

        return dto;
    }
}
