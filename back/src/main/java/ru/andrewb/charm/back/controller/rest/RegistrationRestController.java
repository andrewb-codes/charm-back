package ru.andrewb.charm.back.controller.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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

@Tag(name = "Registration", description = "User registration")
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

    @Operation(summary = "Register user", description = "Creates a new inactive user profile.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "User registered"),
            @ApiResponse(responseCode = "400", description = "Validation failed"),
            @ApiResponse(responseCode = "409", description = "Email is already registered")
    })
    @PostMapping
    public ResponseEntity<?> register(@Valid @RequestBody RegistrationRequest request) {
        Long id = service.save(mapper.map(request));
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("id", id));
    }
}
