package ru.andrewb.charm.back.controller.ui;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import ru.andrewb.charm.back.security.AuthUtils;

import static ru.andrewb.charm.back.web.Urls.LOGIN_URL;
import static ru.andrewb.charm.back.web.Urls.LOGOUT_URL;

@Controller
public class LogoutController {

    @PostMapping(LOGOUT_URL)
    public String logout(HttpServletRequest req) {
        var user = AuthUtils.getUserOrNull(req);
        if (user != null) {
            req.getSession(false).invalidate();
        }
        return "redirect:" + LOGIN_URL;
    }
}
