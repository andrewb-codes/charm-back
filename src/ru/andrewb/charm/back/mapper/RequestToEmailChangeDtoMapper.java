package ru.andrewb.charm.back.mapper;

import jakarta.servlet.http.HttpServletRequest;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.andrewb.charm.back.dto.EmailChangeDto;

import static ru.andrewb.charm.back.utils.Strings.stripToNull;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class RequestToEmailChangeDtoMapper implements Mapper<HttpServletRequest, EmailChangeDto> {

    private static final RequestToEmailChangeDtoMapper INSTANCE = new RequestToEmailChangeDtoMapper();

    public static RequestToEmailChangeDtoMapper getInstance() {
        return INSTANCE;
    }


    @Override
    public EmailChangeDto map(HttpServletRequest req) {
        return map(req, new EmailChangeDto());
    }

    @Override
    public EmailChangeDto map(HttpServletRequest req, EmailChangeDto dto) {
        dto.setNewEmail(stripToNull(req.getParameter("email")));
        dto.setCurrentPassword(stripToNull(req.getParameter("currentPassword")));
        return dto;
    }
}

