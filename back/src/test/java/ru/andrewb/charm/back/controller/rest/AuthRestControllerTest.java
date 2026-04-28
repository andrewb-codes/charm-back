package ru.andrewb.charm.back.controller.rest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import ru.andrewb.charm.back.controller.request.JwtLoginRequest;
import ru.andrewb.charm.back.dto.JwtTokenResponse;
import ru.andrewb.charm.back.model.Role;
import ru.andrewb.charm.back.security.AuthUser;
import ru.andrewb.charm.back.security.JwtService;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthRestControllerTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtService jwtService;

    @Mock
    private Authentication authentication;

    private AuthRestController controller;

    @BeforeEach
    void setUp() {
        controller = new AuthRestController(authenticationManager, jwtService);
    }

    @Test
    void login_shouldReturnTokenResponse_whenCredentialsAreValid() {
        JwtLoginRequest request = new JwtLoginRequest();
        request.setEmail("user@mail.com");
        request.setPassword("123456");

        AuthUser user = new AuthUser(5L, "user@mail.com", "hash", Role.USER);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(user);
        when(jwtService.generateToken(user)).thenReturn("token-value");
        when(jwtService.getAccessTokenTtlSeconds()).thenReturn(3600L);

        ResponseEntity<JwtTokenResponse> response = controller.login(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("token-value", response.getBody().getAccessToken());
        assertEquals("Bearer", response.getBody().getTokenType());
        assertEquals(3600L, response.getBody().getExpiresIn());
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }

    @Test
    void login_shouldReturnUnauthorized_whenCredentialsAreInvalid() {
        JwtLoginRequest request = new JwtLoginRequest();
        request.setEmail("user@mail.com");
        request.setPassword("wrong");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("bad credentials"));

        ResponseEntity<JwtTokenResponse> response = controller.login(request);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNull(response.getBody());
    }
}
