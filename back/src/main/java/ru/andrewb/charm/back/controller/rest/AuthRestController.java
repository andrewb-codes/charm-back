package ru.andrewb.charm.back.controller.rest;

import jakarta.validation.Valid;
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

    @PostMapping(LOGIN_URL)
    public ResponseEntity<JwtTokenResponse> login(@Valid @RequestBody JwtLoginRequest request) {
        try {
            var authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()
                    )
            );

            AuthUser user = (AuthUser) authentication.getPrincipal();
            String token = jwtService.generateToken(user);

            return ResponseEntity.ok(
                    new JwtTokenResponse(token, "Bearer", jwtService.getAccessTokenTtlSeconds())
            );
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }
}
