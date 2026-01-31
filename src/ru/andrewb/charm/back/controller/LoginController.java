package ru.andrewb.charm.back.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import ru.andrewb.charm.back.dto.UserDetailsDto;
import ru.andrewb.charm.back.mapper.RequestToLoginDtoMapper;
import ru.andrewb.charm.back.model.exception.BadRequestException;
import ru.andrewb.charm.back.service.ProfileService;
import ru.andrewb.charm.back.validator.LoginValidator;
import ru.andrewb.charm.back.web.flash.Flash;

import java.io.IOException;

import static ru.andrewb.charm.back.utils.RequestParamUtils.rid;
import static ru.andrewb.charm.back.utils.Urls.LOGIN_URL;
import static ru.andrewb.charm.back.utils.Urls.PROFILE_URL;
import static ru.andrewb.charm.back.utils.Views.getJspPath;

@Slf4j
@WebServlet(LOGIN_URL)
public class LoginController extends HttpServlet {

    private final ProfileService service = ProfileService.getInstance();
    private final LoginValidator loginValidator = LoginValidator.getInstance();
    private final RequestToLoginDtoMapper requestToLoginDtoMapper = RequestToLoginDtoMapper.getInstance();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        var userDetails = (UserDetailsDto) req.getSession().getAttribute("userDetails");
        if (userDetails != null) {
            resp.sendRedirect(req.getContextPath() + PROFILE_URL + "?id=" + userDetails.getId());
            return;
        }

        var flash = Flash.consume(req);
        if (flash != null) {
            if (!flash.getErrors().isEmpty()) {
                req.setAttribute("errors", flash.getErrors());
            }
            if (!flash.getFields().isEmpty()) {
                req.setAttribute("fields", flash.getFields());
            }
        }

        req.getRequestDispatcher(getJspPath(LOGIN_URL)).forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            var dto = requestToLoginDtoMapper.map(req);

            var vr = loginValidator.validate(dto);
            if (vr.isNotValid()) {
                vr.getErrors().forEach(code -> Flash.addError(req, code));
                Flash.putField(req, "email", dto.getEmail());
                resp.sendRedirect(req.getContextPath() + LOGIN_URL);
                return;
            }

            var userDetails = service.login(dto);
            log.info("[{}] Login ok: email={}", rid(req), dto.getEmail());
            req.getSession().setAttribute("userDetails", userDetails);
            resp.sendRedirect(req.getContextPath() + PROFILE_URL + "?id=" + userDetails.getId());

        } catch (BadRequestException e) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        }
    }
}
