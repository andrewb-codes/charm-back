package ru.andrewb.charm.back.mapper;

import jakarta.servlet.http.HttpServletRequest;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.andrewb.charm.back.dto.RegistrationDto;

import static ru.andrewb.charm.back.utils.Strings.stripToNull;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class RequestToRegistrationDtoMapper implements Mapper<HttpServletRequest, RegistrationDto> {

    private static final RequestToRegistrationDtoMapper INSTANCE = new RequestToRegistrationDtoMapper();

    public static RequestToRegistrationDtoMapper getInstance() {
        return INSTANCE;
    }

    @Override
    public RegistrationDto map(HttpServletRequest req) {
        return map(req, new RegistrationDto());
    }

    @Override
    public RegistrationDto map(HttpServletRequest req, RegistrationDto dto) {
        dto.setEmail(stripToNull(req.getParameter("email")));
        dto.setPassword(stripToNull(req.getParameter("password")));
        return dto;
    }
}
