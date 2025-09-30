package ru.andrewb.charm.back.mapper;

import jakarta.servlet.http.HttpServletRequest;
import ru.andrewb.charm.back.dto.RegistrationDto;

public class RequestToRegistrationDtoMapper implements Mapper<HttpServletRequest, RegistrationDto> {

    private static final RequestToRegistrationDtoMapper INSTANCE = new RequestToRegistrationDtoMapper();

    public RequestToRegistrationDtoMapper() {
    }

    public static RequestToRegistrationDtoMapper getInstance() {
        return INSTANCE;
    }

    @Override
    public RegistrationDto map(HttpServletRequest req) {
        return map(req, new RegistrationDto());
    }

    @Override
    public RegistrationDto map(HttpServletRequest req, RegistrationDto dto) {
        String email = req.getParameter("email");
        dto.setEmail(email == null ? null : email.trim());
        String pwd = req.getParameter("password");
        dto.setPassword(pwd == null ? null : pwd.trim());
        return dto;
    }
}
