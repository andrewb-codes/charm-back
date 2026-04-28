package ru.andrewb.charm.back.controller.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.web.bind.annotation.*;
import ru.andrewb.charm.back.controller.request.EmailChangeRequest;
import ru.andrewb.charm.back.controller.request.PasswordChangeRequest;
import ru.andrewb.charm.back.controller.request.ProfileUpdateRequest;
import ru.andrewb.charm.back.mapper.EmailChangeRequestToCommandMapper;
import ru.andrewb.charm.back.mapper.PasswordChangeRequestToCommandMapper;
import ru.andrewb.charm.back.mapper.ProfileUpdateRequestToCommandMapper;
import ru.andrewb.charm.back.security.AuthUser;
import ru.andrewb.charm.back.service.ProfileService;
import ru.andrewb.charm.back.service.command.EmailChangeCommand;
import ru.andrewb.charm.back.service.command.PasswordChangeCommand;
import ru.andrewb.charm.back.service.command.ProfileUpdateCommand;

import static ru.andrewb.charm.back.config.OpenApiConfig.BEARER_AUTH;
import static ru.andrewb.charm.back.web.Urls.PROFILE_REST_URL;

@Tag(name = "Profile", description = "Current user profile")
@SecurityRequirement(name = BEARER_AUTH)
@RestController
@RequestMapping(PROFILE_REST_URL)
public class ProfileRestController {

    private final ProfileService service;
    private final ProfileUpdateRequestToCommandMapper profileUpdateRequestToCommandMapper;
    private final EmailChangeRequestToCommandMapper emailChangeRequestToCommandMapper;
    private final PasswordChangeRequestToCommandMapper passwordChangeRequestToCommandMapper;

    public ProfileRestController(
            ProfileService service,
            ProfileUpdateRequestToCommandMapper profileUpdateRequestToCommandMapper,
            EmailChangeRequestToCommandMapper emailChangeRequestToCommandMapper,
            PasswordChangeRequestToCommandMapper passwordChangeRequestToCommandMapper
    ) {
        this.service = service;
        this.profileUpdateRequestToCommandMapper = profileUpdateRequestToCommandMapper;
        this.emailChangeRequestToCommandMapper = emailChangeRequestToCommandMapper;
        this.passwordChangeRequestToCommandMapper = passwordChangeRequestToCommandMapper;
    }

    @Operation(summary = "Get current profile")
    @GetMapping
    public ResponseEntity<?> getProfile(@Parameter(hidden = true) @AuthenticationPrincipal AuthUser user) {
        return ResponseEntity.ok(service.findByIdOrThrow(user.getId()));
    }

    @Operation(summary = "Update current profile")
    @PutMapping
    public ResponseEntity<?> updateProfile(
            @Parameter(hidden = true) @AuthenticationPrincipal AuthUser user,
            @Valid @RequestBody ProfileUpdateRequest request
    ) {
        ProfileUpdateCommand command = profileUpdateRequestToCommandMapper.map(request);
        service.update(user.getId(), command, null);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Change current user email")
    @PutMapping("/email")
    public ResponseEntity<?> changeEmail(
            @Parameter(hidden = true) @AuthenticationPrincipal AuthUser user,
            @Valid @RequestBody EmailChangeRequest request
    ) {
        EmailChangeCommand command = emailChangeRequestToCommandMapper.map(request);
        service.changeEmail(user.getId(), command);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Change current user password")
    @PutMapping("/password")
    public ResponseEntity<?> changePassword(
            @Parameter(hidden = true) @AuthenticationPrincipal AuthUser user,
            @Valid @RequestBody PasswordChangeRequest request
    ) {
        PasswordChangeCommand command = passwordChangeRequestToCommandMapper.map(request);
        service.changePassword(user.getId(), command);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Delete current profile")
    @DeleteMapping
    public ResponseEntity<?> deleteProfile(
            @Parameter(hidden = true) @AuthenticationPrincipal AuthUser user,
            @Parameter(hidden = true) HttpServletRequest req,
            @Parameter(hidden = true) HttpServletResponse resp
    ) {
        service.delete(user.getId());
        new SecurityContextLogoutHandler().logout(req, resp, null);
        return ResponseEntity.noContent().build();
    }
}
