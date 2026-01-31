package ru.andrewb.charm.back.controller;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import ru.andrewb.charm.back.mapper.RequestToEmailChangeDtoMapper;
import ru.andrewb.charm.back.model.exception.BadRequestException;
import ru.andrewb.charm.back.model.exception.DuplicateEmailException;
import ru.andrewb.charm.back.model.exception.NotFoundException;
import ru.andrewb.charm.back.service.ProfileService;
import ru.andrewb.charm.back.utils.RequestParamUtils;
import ru.andrewb.charm.back.validator.EmailChangeValidator;
import ru.andrewb.charm.back.web.flash.Flash;

import java.io.IOException;

import static ru.andrewb.charm.back.utils.RequestParamUtils.rid;
import static ru.andrewb.charm.back.utils.Urls.EMAIL_URL;
import static ru.andrewb.charm.back.utils.Urls.SETTINGS_URL;

@Slf4j
@WebServlet(EMAIL_URL)
public class EmailChangeController extends HttpServlet {

    private final ProfileService service = ProfileService.getInstance();
    private final EmailChangeValidator emailChangeValidator = EmailChangeValidator.getInstance();
    private final RequestToEmailChangeDtoMapper emailChangeDtoMapper = RequestToEmailChangeDtoMapper.getInstance();

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            long id = RequestParamUtils.requirePositiveLong(req, "id");
            var dto = emailChangeDtoMapper.map(req);

            var vr = emailChangeValidator.validate(dto);
            if (vr.isNotValid()) {
                vr.getErrors().forEach(code -> Flash.addError(req, code));
                Flash.putField(req, "email", dto.getNewEmail());
                resp.sendRedirect(req.getContextPath() + SETTINGS_URL + "?id=" + id);
                return;
            }

            service.changeEmail(id, dto);
            log.info("[{}] Email changed: id={}", rid(req), id);
            resp.sendRedirect(req.getContextPath() + SETTINGS_URL + "?id=" + id);

        } catch (NotFoundException e) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, e.getMessage());
        } catch (BadRequestException e) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } catch (DuplicateEmailException e) {
            resp.sendError(HttpServletResponse.SC_CONFLICT, e.getMessage());
        }
    }
}
