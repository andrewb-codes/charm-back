package ru.andrewb.charm.back.controller.rest;

import com.fasterxml.jackson.databind.DatabindException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import ru.andrewb.charm.back.dto.RegistrationDto;
import ru.andrewb.charm.back.dto.UserDetailsDto;
import ru.andrewb.charm.back.mapper.JsonMapper;
import ru.andrewb.charm.back.mapper.RequestToProfileUpdateDtoMapper;
import ru.andrewb.charm.back.model.exception.BadRequestException;
import ru.andrewb.charm.back.model.exception.DuplicateEmailException;
import ru.andrewb.charm.back.model.exception.NotFoundException;
import ru.andrewb.charm.back.service.ProfileService;
import ru.andrewb.charm.back.utils.RequestParamUtils;
import ru.andrewb.charm.back.validator.ProfileUpdateValidator;
import ru.andrewb.charm.back.validator.RegistrationValidator;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import static ru.andrewb.charm.back.utils.Urls.PROFILE_REST_URL;

@Slf4j
@MultipartConfig
@WebServlet(PROFILE_REST_URL)
public class ProfileController extends HttpServlet {

    private final ProfileService service = ProfileService.getInstance();

    private final ProfileUpdateValidator profileUpdateValidator = ProfileUpdateValidator.getInstance();
    private final RegistrationValidator registrationValidator = RegistrationValidator.getInstance();
    private final RequestToProfileUpdateDtoMapper requestToProfileUpdateDtoMapper = RequestToProfileUpdateDtoMapper.getInstance();
    private final JsonMapper jsonMapper = JsonMapper.getInstance();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json;charset=UTF-8");
        try (PrintWriter w = resp.getWriter()) {
            long id = RequestParamUtils.requirePositiveLong(req, "id");
            var dtoOpt = service.findById(id);
            if (dtoOpt.isPresent()) {
                jsonMapper.writeValue(w, dtoOpt.get());
            } else {
                req.setAttribute("errors", List.of("error.profile.not-found"));
                resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
        } catch (BadRequestException e) {
            req.setAttribute("errors", List.of(e.getMessage()));
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
        } catch (DatabindException e) {
            req.setAttribute("errors", List.of(e.getLocalizedMessage(), e.getOriginalMessage()));
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try (BufferedReader r = req.getReader()) {
            var dto = jsonMapper.readValue(r, RegistrationDto.class);

            var vr = registrationValidator.validate(dto);
            if (vr.isNotValid()) {
                req.setAttribute("errors", vr.getErrors());
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
                return;
            }

            Long id = service.save(dto);
            resp.setStatus(HttpServletResponse.SC_CREATED);
            resp.setHeader("Location",
                    req.getContextPath() + PROFILE_REST_URL + "?id=" + id);
            resp.setContentType("application/json;charset=UTF-8");
            resp.getWriter().write("{\"id\":" + id + "}");
        } catch (DuplicateEmailException e) {
            req.setAttribute("errors", List.of(e.getMessage()));
            resp.sendError(HttpServletResponse.SC_CONFLICT);
        } catch (BadRequestException | DatabindException e) {
            req.setAttribute("errors", List.of(e.getMessage()));
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            long id = RequestParamUtils.requirePositiveLong(req, "id");
            var dto = requestToProfileUpdateDtoMapper.map(req);

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
        } catch (BadRequestException e) {
            req.setAttribute("errors", List.of(e.getMessage()));
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
        } catch (DatabindException e) {
            req.setAttribute("errors", List.of(e.getLocalizedMessage(), e.getOriginalMessage()));
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            long id = RequestParamUtils.requirePositiveLong(req, "id");

            boolean deleted = service.delete(id);
            if (!deleted) {
                req.setAttribute("errors", List.of("error.profile.not-found"));
                resp.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }

            var sessionUser = (UserDetailsDto) req.getSession().getAttribute("userDetails");
            if (sessionUser != null && sessionUser.getId() != null && sessionUser.getId().equals(id)) {
                req.getSession().invalidate();
            }
            resp.setStatus(HttpServletResponse.SC_NO_CONTENT);

        } catch (BadRequestException e) {
            req.setAttribute("errors", List.of(e.getMessage()));
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
        }
    }
}
