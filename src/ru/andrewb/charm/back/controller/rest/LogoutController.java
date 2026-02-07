package ru.andrewb.charm.back.controller.rest;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import ru.andrewb.charm.back.dto.UserDetailsDto;
import ru.andrewb.charm.back.security.AuthUtils;

import java.io.IOException;

import static ru.andrewb.charm.back.web.Urls.LOGOUT_REST_URL;

@Slf4j
@WebServlet(LOGOUT_REST_URL)
public class LogoutController extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        UserDetailsDto user = AuthUtils.getUserOrNull(req);
        if (user != null) {
            req.getSession(false).invalidate();
        }
        resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
    }
}
