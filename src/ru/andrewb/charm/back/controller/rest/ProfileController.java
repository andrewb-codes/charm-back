package ru.andrewb.charm.back.controller.rest;

import com.fasterxml.jackson.databind.DatabindException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import ru.andrewb.charm.back.dto.ProfileUpdateDto;
import ru.andrewb.charm.back.mapper.JsonMapper;
import ru.andrewb.charm.back.model.exception.BadRequestException;
import ru.andrewb.charm.back.model.exception.NotFoundException;
import ru.andrewb.charm.back.security.ProfileAccess;
import ru.andrewb.charm.back.service.ProfileService;
import ru.andrewb.charm.back.validator.ProfileUpdateValidator;
import ru.andrewb.charm.back.validator.RegistrationValidator;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.List;

import static ru.andrewb.charm.back.web.Urls.PROFILE_REST_URL;

@Slf4j
@MultipartConfig
@WebServlet(PROFILE_REST_URL)
public class ProfileController extends HttpServlet {

    private final ProfileService service = ProfileService.getInstance();
    private final ProfileUpdateValidator profileUpdateValidator = ProfileUpdateValidator.getInstance();
    private final RegistrationValidator registrationValidator = RegistrationValidator.getInstance();
    private final JsonMapper jsonMapper = JsonMapper.getInstance();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            var authCtx = ProfileAccess.resolveOrSendRest(req, resp);
            if (authCtx == null) return;

            long id = authCtx.targetId();

            var dto = service.findByIdOrThrow(id);
            resp.setContentType("application/json;charset=UTF-8");
            jsonMapper.writeValue(resp.getWriter(), dto);

        } catch (NotFoundException e) {
            req.setAttribute("errors", List.of(e.getMessage()));
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);

        } catch (DatabindException e) {
            req.setAttribute("errors", List.of("error.param.invalid"));
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST);

        } catch (BadRequestException e) {
            req.setAttribute("errors", List.of(e.getMessage()));
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try (BufferedReader reader = req.getReader()) {
            var authCtx = ProfileAccess.resolveOrSendRest(req, resp);
            if (authCtx == null) return;

            long id = authCtx.targetId();
            var dto = jsonMapper.readValue(reader, ProfileUpdateDto.class);

            var vr = profileUpdateValidator.validate(dto);
            if (vr.isNotValid()) {
                req.setAttribute("errors", vr.getErrors());
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
                return;
            }

            service.update(id, dto);
            resp.setStatus(HttpServletResponse.SC_NO_CONTENT);

        } catch (NotFoundException e) {
            req.setAttribute("errors", List.of(e.getMessage()));
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);

        } catch (DatabindException e) {
            req.setAttribute("errors", List.of("error.param.invalid"));
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST);

        } catch (BadRequestException e) {
            req.setAttribute("errors", List.of(e.getMessage()));
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST);

        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            var authCtx = ProfileAccess.resolveOrSendRest(req, resp);
            if (authCtx == null) return;

            long id = authCtx.targetId();

            boolean deleted = service.delete(id);
            if (!deleted) {
                req.setAttribute("errors", List.of("error.profile.not-found"));
                resp.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }

            if (authCtx.user().getId().equals(id)) {
                req.getSession(false).invalidate();
            }
            resp.setStatus(HttpServletResponse.SC_NO_CONTENT);

        } catch (BadRequestException e) {
            req.setAttribute("errors", List.of(e.getMessage()));
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
        }
    }
}
