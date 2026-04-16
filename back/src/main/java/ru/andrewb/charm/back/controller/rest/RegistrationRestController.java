package ru.andrewb.charm.back.controller.rest;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.andrewb.charm.back.controller.request.RegistrationRequest;
import ru.andrewb.charm.back.mapper.RegistrationRequestToCommandMapper;
import ru.andrewb.charm.back.service.ProfileService;

import java.util.Map;

import static ru.andrewb.charm.back.web.Urls.REGISTRATION_REST_URL;

@RestController
@RequestMapping(REGISTRATION_REST_URL)
public class RegistrationRestController {

    private final ProfileService service;
    private final RegistrationRequestToCommandMapper mapper;

    public RegistrationRestController(
            ProfileService service,
            RegistrationRequestToCommandMapper mapper
    ) {
        this.service = service;
        this.mapper = mapper;
    }

    @PostMapping
    public ResponseEntity<?> register(@Valid @RequestBody RegistrationRequest request) {
        Long id = service.save(mapper.map(request));
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("id", id));
    }
}
