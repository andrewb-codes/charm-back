package ru.andrewb.charm.back.mapper;

import jakarta.servlet.http.HttpServletRequest;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.andrewb.charm.back.dto.LoginDto;

import static ru.andrewb.charm.back.utils.Strings.stripToNull;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class RequestToLoginDtoMapper implements Mapper<HttpServletRequest, LoginDto> {

    private static final RequestToLoginDtoMapper INSTANCE = new RequestToLoginDtoMapper();

    public static RequestToLoginDtoMapper getInstance() {
        return INSTANCE;
    }

    @Override
    public LoginDto map(HttpServletRequest req) {
        return map(req, new LoginDto());
    }

    @Override
    public LoginDto map(HttpServletRequest req, LoginDto dto) {
        dto.setEmail(stripToNull(req.getParameter("email")));
        dto.setPassword(stripToNull(req.getParameter("password")));
        return dto;
    }
}
