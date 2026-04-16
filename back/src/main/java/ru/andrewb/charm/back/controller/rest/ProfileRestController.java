package ru.andrewb.charm.back.controller.rest;

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

import static ru.andrewb.charm.back.web.Urls.PROFILE_REST_URL;

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

    @GetMapping
    public ResponseEntity<?> getProfile(@AuthenticationPrincipal AuthUser user) {
        return ResponseEntity.ok(service.findByIdOrThrow(user.getId()));
    }

    @PutMapping
    public ResponseEntity<?> updateProfile(
            @AuthenticationPrincipal AuthUser user,
            @Valid @RequestBody ProfileUpdateRequest request
    ) {
        ProfileUpdateCommand command = profileUpdateRequestToCommandMapper.map(request);
        service.update(user.getId(), command, null);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/email")
    public ResponseEntity<?> changeEmail(
            @AuthenticationPrincipal AuthUser user,
            @Valid @RequestBody EmailChangeRequest request
    ) {
        EmailChangeCommand command = emailChangeRequestToCommandMapper.map(request);
        service.changeEmail(user.getId(), command);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/password")
    public ResponseEntity<?> changePassword(
            @AuthenticationPrincipal AuthUser user,
            @Valid @RequestBody PasswordChangeRequest request
    ) {
        PasswordChangeCommand command = passwordChangeRequestToCommandMapper.map(request);
        service.changePassword(user.getId(), command);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping
    public ResponseEntity<?> deleteProfile(
            @AuthenticationPrincipal AuthUser user,
            HttpServletRequest req,
            HttpServletResponse resp
    ) {
        service.delete(user.getId());
        new SecurityContextLogoutHandler().logout(req, resp, null);
        return ResponseEntity.noContent().build();
    }
}
