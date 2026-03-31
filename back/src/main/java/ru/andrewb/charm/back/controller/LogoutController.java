package ru.andrewb.charm.back.controller;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import ru.andrewb.charm.back.dto.UserDetailsDto;
import ru.andrewb.charm.back.security.AuthUtils;

import java.io.IOException;

import static ru.andrewb.charm.back.web.RequestParamUtils.rid;
import static ru.andrewb.charm.back.web.Urls.LOGIN_URL;
import static ru.andrewb.charm.back.web.Urls.LOGOUT_URL;

@Slf4j
@WebServlet(LOGOUT_URL)
public class LogoutController extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        UserDetailsDto user = AuthUtils.getUserOrNull(req);
        if (user != null) {
            log.info("[{}] User logout: email={}", rid(req), user.getEmail());
            req.getSession(false).invalidate();
        }
        resp.sendRedirect(req.getContextPath() + LOGIN_URL);
    }
}
