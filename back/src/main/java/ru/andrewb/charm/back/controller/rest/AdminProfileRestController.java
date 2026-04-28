package ru.andrewb.charm.back.controller.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ru.andrewb.charm.back.controller.request.ProfileUpdateRequest;
import ru.andrewb.charm.back.mapper.ProfileUpdateRequestToCommandMapper;
import ru.andrewb.charm.back.service.ProfileService;

import static ru.andrewb.charm.back.config.OpenApiConfig.BEARER_AUTH;
import static ru.andrewb.charm.back.web.Urls.ADMIN_PROFILES_REST_URL;

@Tag(name = "Admin profile", description = "Admin single profile operations")
@SecurityRequirement(name = BEARER_AUTH)
@RestController
@RequestMapping(ADMIN_PROFILES_REST_URL)
@PreAuthorize("hasRole('ADMIN')")
public class AdminProfileRestController {

    private final ProfileService service;
    private final ProfileUpdateRequestToCommandMapper mapper;

    public AdminProfileRestController(
            ProfileService service,
            ProfileUpdateRequestToCommandMapper mapper
    ) {
        this.service = service;
        this.mapper = mapper;
    }

    @Operation(summary = "Get profile by id as admin")
    @GetMapping("/{id}")
    public ResponseEntity<?> getProfile(@Parameter(example = "1") @PathVariable("id") Long id) {
        return ResponseEntity.ok(service.findByIdOrThrow(id));
    }

    @Operation(summary = "Update profile by id as admin")
    @PutMapping("/{id}")
    public ResponseEntity<?> updateProfile(
            @Parameter(example = "1") @PathVariable("id") Long id,
            @Valid @RequestBody ProfileUpdateRequest request
    ) {
        service.update(id, mapper.map(request), null);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Delete profile by id as admin")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteProfile(@Parameter(example = "1") @PathVariable("id") Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
