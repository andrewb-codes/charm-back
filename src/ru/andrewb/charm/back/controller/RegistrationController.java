package ru.andrewb.charm.back.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import ru.andrewb.charm.back.mapper.RequestToRegistrationDtoMapper;
import ru.andrewb.charm.back.model.exception.BadRequestException;
import ru.andrewb.charm.back.model.exception.DuplicateEmailException;
import ru.andrewb.charm.back.service.ProfileService;
import ru.andrewb.charm.back.validator.RegistrationValidator;
import ru.andrewb.charm.back.web.flash.Flash;

import java.io.IOException;

import static ru.andrewb.charm.back.utils.RequestParams.rid;
import static ru.andrewb.charm.back.utils.UrlUtils.*;

@WebServlet(REGISTRATION_URL)
@Slf4j
public class RegistrationController extends HttpServlet {

    private final ProfileService service = ProfileService.getInstance();
    private final RegistrationValidator registrationValidator = RegistrationValidator.getInstance();
    private final RequestToRegistrationDtoMapper requestToRegistrationDtoMapper = RequestToRegistrationDtoMapper.getInstance();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        var flash = Flash.consume(req);
        if (flash != null) {
            if (!flash.getErrors().isEmpty()) {
                req.setAttribute("errors", flash.getErrors());
            }
            if (!flash.getFields().isEmpty()) {
                req.setAttribute("fields", flash.getFields());
            }
        }

        req.getRequestDispatcher(getJspPath("/registration")).forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            var dto = requestToRegistrationDtoMapper.map(req);

            var vr = registrationValidator.validate(dto);
            if (vr.isNotValid()) {
                vr.getErrors().forEach(code -> Flash.addError(req, code));
                Flash.putField(req, "email", dto.getEmail());
                resp.sendRedirect(req.getContextPath() + REGISTRATION_URL);
                return;
            }

            service.save(dto);
            log.info("[{}] Registration ok: email={}", rid(req), dto.getEmail());
            resp.sendRedirect(req.getContextPath() + LOGIN_URL);

        } catch (BadRequestException e) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } catch (DuplicateEmailException e) {
            resp.sendError(HttpServletResponse.SC_CONFLICT, e.getMessage());
        }
    }
}
