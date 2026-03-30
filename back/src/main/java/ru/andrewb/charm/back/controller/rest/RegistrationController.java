package ru.andrewb.charm.back.controller.rest;

import com.fasterxml.jackson.databind.DatabindException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import ru.andrewb.charm.back.service.ProfileService;
import ru.andrewb.charm.back.dto.RegistrationDto;
import ru.andrewb.charm.back.mapper.JsonMapper;
import ru.andrewb.charm.back.model.exception.BadRequestException;
import ru.andrewb.charm.back.model.exception.DuplicateEmailException;
import ru.andrewb.charm.back.validator.RegistrationValidator;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import static ru.andrewb.charm.back.web.Urls.PROFILE_REST_URL;
import static ru.andrewb.charm.back.web.Urls.REGISTRATION_REST_URL;

@WebServlet(REGISTRATION_REST_URL)
public class RegistrationController extends HttpServlet {

    private final ProfileService service = ProfileService.getInstance();
    private final RegistrationValidator registrationValidator = RegistrationValidator.getInstance();
    private final JsonMapper jsonMapper = JsonMapper.getInstance();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try (BufferedReader reader = req.getReader()) {
            var dto = jsonMapper.readValue(reader, RegistrationDto.class);

            var vr = registrationValidator.validate(dto);
            if (vr.isNotValid()) {
                req.setAttribute("errors", vr.getErrors());
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
                return;
            }

            Long id = service.save(dto);
            resp.setStatus(HttpServletResponse.SC_CREATED);
            resp.setHeader("Location", req.getContextPath() + PROFILE_REST_URL + "?id=" + id);
            resp.setContentType("application/json;charset=UTF-8");
            jsonMapper.writeValue(resp.getWriter(), Map.of("id", id));

        } catch (DuplicateEmailException e) {
            req.setAttribute("errors", List.of(e.getMessage()));
            resp.sendError(HttpServletResponse.SC_CONFLICT);

        } catch (DatabindException e) {
            req.setAttribute("errors", List.of("error.param.invalid"));
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST);

        } catch (BadRequestException e) {
            req.setAttribute("errors", List.of(e.getMessage()));
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
        }
    }
}
