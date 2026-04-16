package ru.andrewb.charm.back.controller.rest;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ru.andrewb.charm.back.controller.request.ProfileUpdateRequest;
import ru.andrewb.charm.back.mapper.ProfileUpdateRequestToCommandMapper;
import ru.andrewb.charm.back.service.ProfileService;

import static ru.andrewb.charm.back.web.Urls.ADMIN_PROFILES_REST_URL;

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

    @GetMapping("/{id}")
    public ResponseEntity<?> getProfile(@PathVariable("id") Long id) {
        return ResponseEntity.ok(service.findByIdOrThrow(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateProfile(
            @PathVariable("id") Long id,
            @Valid @RequestBody ProfileUpdateRequest request
    ) {
        service.update(id, mapper.map(request), null);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteProfile(@PathVariable("id") Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
