package ru.andrewb.charm.back.controller.rest;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.andrewb.charm.back.security.AuthUtils;

import static ru.andrewb.charm.back.web.Urls.LOGOUT_REST_URL;

@RestController
public class LogoutRestController {

    @PostMapping(LOGOUT_REST_URL)
    public ResponseEntity<Void> logout(HttpServletRequest req) {
        var user = AuthUtils.getUserOrNull(req);
        if (user != null) {
            req.getSession(false).invalidate();
        }
        return ResponseEntity.noContent().build();
    }
}
