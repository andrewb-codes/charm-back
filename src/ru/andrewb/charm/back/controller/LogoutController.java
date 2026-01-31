package ru.andrewb.charm.back.controller;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import ru.andrewb.charm.back.dto.UserDetailsDto;

import java.io.IOException;

import static ru.andrewb.charm.back.utils.RequestParamUtils.rid;
import static ru.andrewb.charm.back.utils.Urls.LOGIN_URL;
import static ru.andrewb.charm.back.utils.Urls.LOGOUT_URL;

@Slf4j
@WebServlet(LOGOUT_URL)
public class LogoutController extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        var userDetails = (UserDetailsDto) req.getSession().getAttribute("userDetails");
        if (userDetails != null) {
            log.info("[{}] User logout: email={}", rid(req), userDetails.getEmail());
        }
        req.getSession().invalidate();
        resp.sendRedirect(req.getContextPath() + LOGIN_URL);
    }
}
