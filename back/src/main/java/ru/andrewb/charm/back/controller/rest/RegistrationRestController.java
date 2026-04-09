package ru.andrewb.charm.back.controller.rest;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import ru.andrewb.charm.back.dto.RegistrationDto;
import ru.andrewb.charm.back.service.ProfileService;
import ru.andrewb.charm.back.validator.RegistrationValidator;

import java.net.URI;
import java.util.Map;

import static ru.andrewb.charm.back.web.Urls.PROFILE_REST_URL;
import static ru.andrewb.charm.back.web.Urls.REGISTRATION_REST_URL;

@RestController
public class RegistrationRestController {

    private final ProfileService service;
    private final RegistrationValidator validator;

    public RegistrationRestController(
            ProfileService service,
            RegistrationValidator validator
    ) {
        this.service = service;
        this.validator = validator;
    }

    @PostMapping(REGISTRATION_REST_URL)
    public ResponseEntity<?> register(
            @RequestBody RegistrationDto dto,
            HttpServletRequest req
    ) {
        var vr = validator.validate(dto);
        if (vr.isNotValid()) {
            return ResponseEntity.badRequest().body(Map.of("errors", vr.getErrors()));
        }

        Long id = service.save(dto);
        URI location = URI.create(req.getContextPath() + PROFILE_REST_URL + "?id=" + id);
        return ResponseEntity.created(location).body(Map.of("id", id));
    }
}
