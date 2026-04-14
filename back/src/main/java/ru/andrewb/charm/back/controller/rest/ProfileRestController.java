package ru.andrewb.charm.back.controller.rest;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.web.bind.annotation.*;
import ru.andrewb.charm.back.dto.EmailChangeDto;
import ru.andrewb.charm.back.dto.PasswordChangeDto;
import ru.andrewb.charm.back.dto.ProfileUpdateDto;
import ru.andrewb.charm.back.security.AuthUser;
import ru.andrewb.charm.back.service.ProfileService;
import ru.andrewb.charm.back.validator.EmailChangeValidator;
import ru.andrewb.charm.back.validator.PasswordChangeValidator;
import ru.andrewb.charm.back.validator.ProfileUpdateValidator;

import java.util.Map;

import static ru.andrewb.charm.back.web.Urls.PROFILE_REST_URL;

@RestController
@RequestMapping(PROFILE_REST_URL)
public class ProfileRestController {

    private final ProfileService service;
    private final ProfileUpdateValidator profileUpdateValidator;
    private final EmailChangeValidator emailChangeValidator;
    private final PasswordChangeValidator passwordChangeValidator;

    public ProfileRestController(
            ProfileService service,
            ProfileUpdateValidator profileUpdateValidator,
            EmailChangeValidator emailChangeValidator,
            PasswordChangeValidator passwordChangeValidator
    ) {
        this.service = service;
        this.profileUpdateValidator = profileUpdateValidator;
        this.emailChangeValidator = emailChangeValidator;
        this.passwordChangeValidator = passwordChangeValidator;
    }

    @GetMapping
    public ResponseEntity<?> getProfile(@AuthenticationPrincipal AuthUser user) {
        return ResponseEntity.ok(service.findByIdOrThrow(user.getId()));
    }

    @PutMapping
    public ResponseEntity<?> updateProfile(
            @AuthenticationPrincipal AuthUser user,
            @RequestBody ProfileUpdateDto dto
    ) {
        var vr = profileUpdateValidator.validate(dto);
        if (vr.isNotValid()) {
            return ResponseEntity.badRequest().body(Map.of("errors", vr.getErrors()));
        }

        service.update(user.getId(), dto, null);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/email")
    public ResponseEntity<?> changeEmail(
            @AuthenticationPrincipal AuthUser user,
            @RequestBody EmailChangeDto dto
    ) {
        var vr = emailChangeValidator.validate(dto);
        if (vr.isNotValid()) {
            return ResponseEntity.badRequest().body(Map.of("errors", vr.getErrors()));
        }

        service.changeEmail(user.getId(), dto);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/password")
    public ResponseEntity<?> changePassword(
            @AuthenticationPrincipal AuthUser user,
            @RequestBody PasswordChangeDto dto
    ) {
        var vr = passwordChangeValidator.validate(dto);
        if (vr.isNotValid()) {
            return ResponseEntity.badRequest().body(Map.of("errors", vr.getErrors()));
        }

        service.changePassword(user.getId(), dto);
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
