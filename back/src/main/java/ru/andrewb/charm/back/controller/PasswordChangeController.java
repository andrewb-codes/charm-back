package ru.andrewb.charm.back.controller;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import ru.andrewb.charm.back.bootstrap.AppComponents;
import ru.andrewb.charm.back.service.ProfileService;
import ru.andrewb.charm.back.dto.UserDetailsDto;
import ru.andrewb.charm.back.mapper.RequestToPasswordChangeDtoMapper;
import ru.andrewb.charm.back.model.exception.BadRequestException;
import ru.andrewb.charm.back.model.exception.NotFoundException;
import ru.andrewb.charm.back.security.AuthUtils;
import ru.andrewb.charm.back.validator.PasswordChangeValidator;
import ru.andrewb.charm.back.web.flash.Flash;

import java.io.IOException;

import static ru.andrewb.charm.back.web.RequestParamUtils.rid;
import static ru.andrewb.charm.back.web.Urls.*;

@Slf4j
@WebServlet(PASSWORD_URL)
public class PasswordChangeController extends HttpServlet {

    private final ProfileService service = AppComponents.PROFILE_SERVICE;
    private final PasswordChangeValidator passwordChangeValidator = PasswordChangeValidator.getInstance();
    private final RequestToPasswordChangeDtoMapper passwordChangeDtoMapper = RequestToPasswordChangeDtoMapper.getInstance();

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            UserDetailsDto user = AuthUtils.getUserOrNull(req);
            if (user == null) {
                resp.sendRedirect(req.getContextPath() + LOGIN_URL);
                return;
            }

            long id = user.getId();
            var dto = passwordChangeDtoMapper.map(req);

            var vr = passwordChangeValidator.validate(dto);
            if (vr.isNotValid()) {
                vr.getErrors().forEach(code -> Flash.addError(req, code));
                resp.sendRedirect(req.getContextPath() + SETTINGS_URL);
                return;
            }

            service.changePassword(id, dto);
            log.info("[{}] Password changed: id={}", rid(req), id);
            resp.sendRedirect(req.getContextPath() + SETTINGS_URL);

        } catch (BadRequestException e) {
            Flash.addError(req, e.getMessage());
            resp.sendRedirect(req.getContextPath() + SETTINGS_URL);

        } catch (NotFoundException e) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, e.getMessage());
        }
    }
}
