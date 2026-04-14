package ru.andrewb.charm.back.controller.rest;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.andrewb.charm.back.dto.RegistrationDto;
import ru.andrewb.charm.back.service.ProfileService;
import ru.andrewb.charm.back.validator.RegistrationValidator;

import java.util.Map;

import static ru.andrewb.charm.back.web.Urls.REGISTRATION_REST_URL;

@RestController
@RequestMapping(REGISTRATION_REST_URL)
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

    @PostMapping
    public ResponseEntity<?> register(@RequestBody RegistrationDto dto) {
        var vr = validator.validate(dto);
        if (vr.isNotValid()) {
            return ResponseEntity.badRequest().body(Map.of("errors", vr.getErrors()));
        }

        Long id = service.save(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("id", id));
    }
}
