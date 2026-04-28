package ru.andrewb.charm.back.controller.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.andrewb.charm.back.controller.request.JwtLoginRequest;
import ru.andrewb.charm.back.dto.JwtTokenResponse;
import ru.andrewb.charm.back.security.AuthUser;
import ru.andrewb.charm.back.security.JwtService;

import static ru.andrewb.charm.back.web.Urls.AUTH_REST_URL;
import static ru.andrewb.charm.back.web.Urls.LOGIN_URL;

@Slf4j
@Tag(name = "Auth", description = "JWT authentication")
@RestController
@RequestMapping(AUTH_REST_URL)
public class AuthRestController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthRestController(
            AuthenticationManager authenticationManager,
            JwtService jwtService
    ) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    @Operation(summary = "Login", description = "Authenticates a user and returns a JWT access token.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Login successful"),
            @ApiResponse(responseCode = "400", description = "Invalid request body"),
            @ApiResponse(responseCode = "401", description = "Invalid credentials")
    })
    @PostMapping(LOGIN_URL)
    public ResponseEntity<JwtTokenResponse> login(@Valid @RequestBody JwtLoginRequest request) {
        log.info("REST login attempt email={}", request.getEmail());
        try {
            var authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()
                    )
            );

            AuthUser user = (AuthUser) authentication.getPrincipal();
            String token = jwtService.generateToken(user);
            log.info("REST login succeeded userId={} email={}", user.getId(), user.getUsername());

            return ResponseEntity.ok(
                    new JwtTokenResponse(token, "Bearer", jwtService.getAccessTokenTtlSeconds())
            );
        } catch (BadCredentialsException e) {
            log.warn("REST login failed email={}", request.getEmail());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }
}
