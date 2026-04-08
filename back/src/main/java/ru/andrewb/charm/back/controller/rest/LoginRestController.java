package ru.andrewb.charm.back.controller.rest;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import ru.andrewb.charm.back.dto.LoginDto;
import ru.andrewb.charm.back.model.exception.BadRequestException;
import ru.andrewb.charm.back.service.ProfileService;
import ru.andrewb.charm.back.validator.LoginValidator;

import java.util.List;
import java.util.Map;

import static ru.andrewb.charm.back.web.Urls.LOGIN_REST_URL;

@RestController
public class LoginRestController {

    private final ProfileService service;
    private final LoginValidator loginValidator;

    public LoginRestController(
            ProfileService service,
            LoginValidator loginValidator
    ) {
        this.service = service;
        this.loginValidator = loginValidator;
    }

    @PostMapping(LOGIN_REST_URL)
    public ResponseEntity<?> login(
            @RequestBody LoginDto dto,
            HttpServletRequest req
    ) {
        var vr = loginValidator.validate(dto);
        if (vr.isNotValid()) {
            return ResponseEntity.badRequest().body(Map.of("errors", vr.getErrors()));
        }

        try {
            var userDetails = service.login(dto);
            req.getSession().setAttribute("userDetails", userDetails);
            return ResponseEntity.noContent().build();

        } catch (BadRequestException e) {
            return ResponseEntity.badRequest().body(Map.of("errors", List.of(e.getMessage())));
        }
    }
}
