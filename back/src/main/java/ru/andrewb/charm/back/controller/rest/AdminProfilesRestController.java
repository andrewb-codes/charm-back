package ru.andrewb.charm.back.controller.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.andrewb.charm.back.controller.request.ProfilesFilterRequest;
import ru.andrewb.charm.back.dto.ProfilesFilter;
import ru.andrewb.charm.back.mapper.ProfilesFilterRequestToProfileFilterMapper;
import ru.andrewb.charm.back.service.ProfileService;

import static ru.andrewb.charm.back.config.OpenApiConfig.BEARER_AUTH;
import static ru.andrewb.charm.back.web.Urls.ADMIN_PROFILES_REST_URL;

@Tag(name = "Admin profiles", description = "Admin profile search")
@SecurityRequirement(name = BEARER_AUTH)
@RestController
@RequestMapping(ADMIN_PROFILES_REST_URL)
@PreAuthorize("hasRole('ADMIN')")
public class AdminProfilesRestController {

    private final ProfileService service;
    private final ProfilesFilterRequestToProfileFilterMapper mapper;

    public AdminProfilesRestController(
            ProfileService service,
            ProfilesFilterRequestToProfileFilterMapper mapper
    ) {
        this.service = service;
        this.mapper = mapper;
    }

    @Operation(summary = "Search profiles as admin")
    @GetMapping
    public ResponseEntity<?> getProfiles(@Valid @ModelAttribute ProfilesFilterRequest request) {
        ProfilesFilter filter = mapper.map(request);
        return ResponseEntity.ok(service.findAll(filter));
    }
}
