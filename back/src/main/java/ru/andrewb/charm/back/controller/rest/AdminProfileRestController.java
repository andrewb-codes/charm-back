package ru.andrewb.charm.back.controller.rest;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ru.andrewb.charm.back.dto.ProfileUpdateDto;
import ru.andrewb.charm.back.service.ProfileService;
import ru.andrewb.charm.back.validator.ProfileUpdateValidator;

import java.util.Map;

import static ru.andrewb.charm.back.web.Urls.ADMIN_PROFILES_REST_URL;

@RestController
@RequestMapping(ADMIN_PROFILES_REST_URL)
@PreAuthorize("hasRole('ADMIN')")
public class AdminProfileRestController {

    private final ProfileService service;
    private final ProfileUpdateValidator validator;

    public AdminProfileRestController(
            ProfileService service,
            ProfileUpdateValidator validator
    ) {
        this.service = service;
        this.validator = validator;
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getProfile(@PathVariable("id") Long id) {
        return ResponseEntity.ok(service.findByIdOrThrow(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateProfile(
            @PathVariable("id") Long id,
            @RequestBody ProfileUpdateDto dto
    ) {
        var vr = validator.validate(dto);
        if (vr.isNotValid()) {
            return ResponseEntity.badRequest().body(Map.of("errors", vr.getErrors()));
        }

        service.update(id, dto, null);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteProfile(@PathVariable("id") Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
