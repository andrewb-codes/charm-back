package ru.andrewb.charm.back.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import ru.andrewb.charm.back.mapper.RequestToPasswordChangeDtoMapper;
import ru.andrewb.charm.back.model.exception.BadRequestException;
import ru.andrewb.charm.back.model.exception.NotFoundException;
import ru.andrewb.charm.back.service.ProfileService;
import ru.andrewb.charm.back.utils.RequestParams;
import ru.andrewb.charm.back.validator.PasswordChangeValidator;
import ru.andrewb.charm.back.web.flash.Flash;

import java.io.IOException;

import static ru.andrewb.charm.back.utils.RequestParams.rid;

@WebServlet("/password")
@Slf4j
public class PasswordChangeController extends HttpServlet {

    private final ProfileService service = ProfileService.getInstance();
    private final PasswordChangeValidator passwordChangeValidator = PasswordChangeValidator.getInstance();
    private final RequestToPasswordChangeDtoMapper passwordChangeDtoMapper = RequestToPasswordChangeDtoMapper.getInstance();

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            long id = RequestParams.requirePositiveLong(req, "id");
            var dto = passwordChangeDtoMapper.map(req);

            var vr = passwordChangeValidator.validate(dto);

            if (vr.isNotValid()) {
                vr.getErrors().forEach(code -> Flash.addError(req, code));
                resp.sendRedirect(req.getContextPath() + "/settings?id=" + id);
                return;
            }

            service.changePassword(id, dto);
            log.info("[{}] Password changed: id={}", rid(req), id);
            resp.sendRedirect(req.getContextPath() + "/settings?id=" + id);

        } catch (NotFoundException e) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, e.getMessage());
        } catch (BadRequestException e) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        }
    }
}
