package ru.andrewb.charm.back.controller;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import ru.andrewb.charm.back.bootstrap.AppComponents;
import ru.andrewb.charm.back.service.ProfileService;
import ru.andrewb.charm.back.dto.UserDetailsDto;
import ru.andrewb.charm.back.mapper.RequestToEmailChangeDtoMapper;
import ru.andrewb.charm.back.model.exception.BadRequestException;
import ru.andrewb.charm.back.model.exception.DuplicateEmailException;
import ru.andrewb.charm.back.model.exception.NotFoundException;
import ru.andrewb.charm.back.security.AuthUtils;
import ru.andrewb.charm.back.validator.EmailChangeValidator;
import ru.andrewb.charm.back.web.flash.Flash;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static ru.andrewb.charm.back.web.RequestParamUtils.rid;
import static ru.andrewb.charm.back.web.Urls.*;

@Slf4j
@WebServlet(EMAIL_URL)
public class EmailChangeController extends HttpServlet {

    private final ProfileService service = AppComponents.PROFILE_SERVICE;
    private final EmailChangeValidator emailChangeValidator = EmailChangeValidator.getInstance();
    private final RequestToEmailChangeDtoMapper emailChangeDtoMapper = RequestToEmailChangeDtoMapper.getInstance();

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            UserDetailsDto user = AuthUtils.getUserOrNull(req);
            if (user == null) {
                resp.sendRedirect(req.getContextPath() + LOGIN_URL);
                return;
            }

            long id = user.getId();
            var dto = emailChangeDtoMapper.map(req);

            var vr = emailChangeValidator.validate(dto);
            if (vr.isNotValid()) {
                vr.getErrors().forEach(code -> Flash.addError(req, code));
                Flash.putField(req, "email", dto.getNewEmail());
                resp.sendRedirect(req.getContextPath() + SETTINGS_URL);
                return;
            }

            service.changeEmail(id, dto);
            log.info("[{}] Email changed: id={}", rid(req), id);
            resp.sendRedirect(req.getContextPath() + SETTINGS_URL);

        }  catch (BadRequestException | DuplicateEmailException e) {
            Flash.addError(req, e.getMessage());
            resp.sendRedirect(req.getContextPath() + SETTINGS_URL);

        } catch (NotFoundException e) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, e.getMessage());
        }
    }
}
