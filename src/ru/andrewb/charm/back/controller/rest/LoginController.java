package ru.andrewb.charm.back.controller.rest;

import com.fasterxml.jackson.databind.DatabindException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import ru.andrewb.charm.back.dto.LoginDto;
import ru.andrewb.charm.back.mapper.JsonMapper;
import ru.andrewb.charm.back.model.exception.BadRequestException;
import ru.andrewb.charm.back.service.ProfileService;
import ru.andrewb.charm.back.validator.LoginValidator;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.List;

import static ru.andrewb.charm.back.web.Urls.LOGIN_REST_URL;

@Slf4j
@WebServlet(LOGIN_REST_URL)
public class LoginController extends HttpServlet {

    private final ProfileService service = ProfileService.getInstance();
    private final LoginValidator loginValidator = LoginValidator.getInstance();
    private final JsonMapper jsonMapper = JsonMapper.getInstance();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try (BufferedReader reader = req.getReader()) {
            var dto = jsonMapper.readValue(reader, LoginDto.class);

            var vr = loginValidator.validate(dto);
            if (vr.isNotValid()) {
                req.setAttribute("errors", vr.getErrors());
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
                return;
            }

            var userDetails = service.login(dto);
            req.getSession().setAttribute("userDetails", userDetails);
            resp.setStatus(HttpServletResponse.SC_NO_CONTENT);

        } catch (DatabindException e) {
            req.setAttribute("errors", List.of("error.param.invalid"));
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
        } catch (BadRequestException e) {
            req.setAttribute("errors", List.of(e.getMessage()));
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST);

        }
    }
}
