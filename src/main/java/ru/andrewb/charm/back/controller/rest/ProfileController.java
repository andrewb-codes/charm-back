package ru.andrewb.charm.back.controller.rest;

import com.fasterxml.jackson.databind.DatabindException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import ru.andrewb.charm.back.dto.EmailChangeDto;
import ru.andrewb.charm.back.dto.PasswordChangeDto;
import ru.andrewb.charm.back.model.exception.*;
import ru.andrewb.charm.back.service.ProfileService;
import ru.andrewb.charm.back.dto.ProfileUpdateDto;
import ru.andrewb.charm.back.mapper.JsonMapper;
import ru.andrewb.charm.back.security.ProfileAccess;
import ru.andrewb.charm.back.validator.EmailChangeValidator;
import ru.andrewb.charm.back.validator.PasswordChangeValidator;
import ru.andrewb.charm.back.validator.ProfileUpdateValidator;
import ru.andrewb.charm.back.validator.RegistrationValidator;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.Buffer;
import java.util.List;

import static ru.andrewb.charm.back.web.Urls.PROFILE_REST_URL;

@Slf4j
@MultipartConfig
@WebServlet(PROFILE_REST_URL + "/*")
public class ProfileController extends HttpServlet {

    private final ProfileService service = ProfileService.getInstance();

    private final ProfileUpdateValidator profileUpdateValidator = ProfileUpdateValidator.getInstance();
    private final EmailChangeValidator emailChangeValidator = EmailChangeValidator.getInstance();
    private final PasswordChangeValidator passwordChangeValidator = PasswordChangeValidator.getInstance();

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

            String pathInfo = req.getPathInfo();
            if (pathInfo == null || pathInfo.isBlank() || "/".equals(pathInfo)) {
                handleProfileUpdate(id, reader, req, resp);
                return;
            }

            switch (pathInfo) {
                case "/email" -> handleEmailChange(id, reader, req, resp);
                case "/password" -> handlePasswordChange(id, reader, req, resp);
                default -> resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            }

        } catch (StorageException e) {
            req.setAttribute("errors", List.of(e.getMessage()));
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

        } catch (NotFoundException e) {
            req.setAttribute("errors", List.of(e.getMessage()));
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);

        } catch (DatabindException e) {
            req.setAttribute("errors", List.of("error.param.invalid"));
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST);

        } catch (OptimisticLockException e) {
            req.setAttribute("errors", List.of("error.optimistic-lock"));
            resp.sendError(HttpServletResponse.SC_CONFLICT);

        } catch (BadRequestException e) {
            req.setAttribute("errors", List.of(e.getMessage()));
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST);

        } catch (DuplicateEmailException e) {
            req.setAttribute("errors", List.of(e.getMessage()));
            resp.sendError(HttpServletResponse.SC_CONFLICT);
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

    private void handleProfileUpdate(long id, BufferedReader reader, HttpServletRequest req, HttpServletResponse resp)
            throws IOException {

        var dto = jsonMapper.readValue(reader, ProfileUpdateDto.class);

        var vr = profileUpdateValidator.validate(dto);
        if (vr.isNotValid()) {
            req.setAttribute("errors", vr.getErrors());
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        service.update(id, dto);
        resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
    }

    private void handleEmailChange(long id, BufferedReader reader, HttpServletRequest req, HttpServletResponse resp)
            throws IOException {

        var dto = jsonMapper.readValue(reader, EmailChangeDto.class);

        var vr = emailChangeValidator.validate(dto);
        if (vr.isNotValid()) {
            req.setAttribute("errors", vr.getErrors());
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        service.changeEmail(id, dto);
        resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
    }

    private void handlePasswordChange(long id, BufferedReader reader, HttpServletRequest req, HttpServletResponse resp)
            throws IOException {

        var dto = jsonMapper.readValue(reader, PasswordChangeDto.class);

        var vr = passwordChangeValidator.validate(dto);
        if (vr.isNotValid()) {
            req.setAttribute("errors", vr.getErrors());
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        service.changePassword(id, dto);
        resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
    }
}
