package ru.andrewb.charm.back.mapper;

import jakarta.servlet.http.HttpServletRequest;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.andrewb.charm.back.dto.PasswordChangeDto;

import static ru.andrewb.charm.back.utils.StringUtils.stripToNull;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class RequestToPasswordChangeDtoMapper implements Mapper<HttpServletRequest, PasswordChangeDto> {

    private static final RequestToPasswordChangeDtoMapper INSTANCE = new RequestToPasswordChangeDtoMapper();

    public static RequestToPasswordChangeDtoMapper getInstance() {
        return INSTANCE;
    }

    @Override
    public PasswordChangeDto map(HttpServletRequest req) {
        return map(req, new PasswordChangeDto());
    }

    @Override
    public PasswordChangeDto map(HttpServletRequest req, PasswordChangeDto dto) {
        dto.setVersion(Integer.parseInt(req.getParameter("version")));
        dto.setCurrentPassword(stripToNull(req.getParameter("currentPassword")));
        dto.setNewPassword(stripToNull(req.getParameter("newPassword")));
        dto.setConfirmPassword(stripToNull(req.getParameter("confirmPassword")));
        return dto;
    }
}
