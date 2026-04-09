package ru.andrewb.charm.back.controller.rest;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.andrewb.charm.back.dto.EmailChangeDto;
import ru.andrewb.charm.back.dto.PasswordChangeDto;
import ru.andrewb.charm.back.dto.ProfileUpdateDto;
import ru.andrewb.charm.back.model.exception.NotFoundException;
import ru.andrewb.charm.back.security.AuthUtils;
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
    public ResponseEntity<?> getProfile(HttpServletRequest req) {
        var authCtx = AuthUtils.getAuthCtx(req);
        if (authCtx == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        if (!authCtx.isAdmin() && authCtx.targetId() != authCtx.user().getId()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        long id = authCtx.targetId();
        return ResponseEntity.ok(service.findByIdOrThrow(id));
    }

    @PutMapping
    public ResponseEntity<?> updateProfile(
            @RequestBody ProfileUpdateDto dto,
            HttpServletRequest req
    ) {
        var authCtx = AuthUtils.getAuthCtx(req);
        if (authCtx == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        if (!authCtx.isAdmin() && authCtx.targetId() != authCtx.user().getId()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        var vr = profileUpdateValidator.validate(dto);
        if (vr.isNotValid()) {
            return ResponseEntity.badRequest().body(Map.of("errors", vr.getErrors()));
        }

        service.update(authCtx.targetId(), dto, null);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/email")
    public ResponseEntity<?> changeEmail(
            @RequestBody EmailChangeDto dto,
            HttpServletRequest req
    ) {
        var authCtx = AuthUtils.getAuthCtx(req);
        if (authCtx == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        if (!authCtx.isAdmin() && authCtx.targetId() != authCtx.user().getId()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        var vr = emailChangeValidator.validate(dto);
        if (vr.isNotValid()) {
            return ResponseEntity.badRequest().body(Map.of("errors", vr.getErrors()));
        }

        service.changeEmail(authCtx.targetId(), dto);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/password")
    public ResponseEntity<?> changePassword(
            @RequestBody PasswordChangeDto dto,
            HttpServletRequest req
    ) {
        var authCtx = AuthUtils.getAuthCtx(req);
        if (authCtx == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        if (!authCtx.isAdmin() && authCtx.targetId() != authCtx.user().getId()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        var vr = passwordChangeValidator.validate(dto);
        if (vr.isNotValid()) {
            return ResponseEntity.badRequest().body(Map.of("errors", vr.getErrors()));
        }

        service.changePassword(authCtx.targetId(), dto);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping
    public ResponseEntity<?> deleteProfile(HttpServletRequest req) {
        var authCtx = AuthUtils.getAuthCtx(req);
        if (authCtx == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        if (!authCtx.isAdmin() && authCtx.targetId() != authCtx.user().getId()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        long id = authCtx.targetId();
        boolean deleted = service.delete(id);
        if (!deleted) {
            throw new NotFoundException("error.profile.not-found");
        }

        if (authCtx.user().getId().equals(id) && req.getSession(false) != null) {
            req.getSession(false).invalidate();
        }

        return ResponseEntity.noContent().build();
    }
}
