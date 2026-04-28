package ru.andrewb.charm.back.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import ru.andrewb.charm.back.model.Role;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthUserDetailsService authUserDetailsService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    private JwtAuthenticationFilter filter;

    @BeforeEach
    void setUp() {
        filter = new JwtAuthenticationFilter(jwtService, authUserDetailsService);
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void doFilterInternal_shouldSkipAuth_whenHeaderIsMissing() throws Exception {
        when(request.getHeader("Authorization")).thenReturn(null);

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(jwtService);
        verifyNoInteractions(authUserDetailsService);
    }

    @Test
    void doFilterInternal_shouldSkipAuth_whenTokenIsInvalid() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Bearer bad-token");
        when(request.getMethod()).thenReturn("GET");
        when(request.getRequestURI()).thenReturn("/api/v1/profile");
        when(jwtService.isValid("bad-token")).thenReturn(false);

        filter.doFilterInternal(request, response, filterChain);

        verify(jwtService).isValid("bad-token");
        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(authUserDetailsService);
    }

    @Test
    void doFilterInternal_shouldAuthenticateUser_whenTokenIsValid() throws Exception {
        AuthUser user = new AuthUser(10L, "user@mail.com", "hash", Role.USER);
        when(request.getHeader("Authorization")).thenReturn("Bearer valid-token");
        when(request.getMethod()).thenReturn("GET");
        when(request.getRequestURI()).thenReturn("/api/v1/profile");
        when(jwtService.isValid("valid-token")).thenReturn(true);
        when(jwtService.extractEmail("valid-token")).thenReturn("user@mail.com");
        when(authUserDetailsService.loadUserByUsername("user@mail.com")).thenReturn(user);

        filter.doFilterInternal(request, response, filterChain);

        var authentication = SecurityContextHolder.getContext().getAuthentication();
        assertSame(user, authentication.getPrincipal());
        assertEquals("ROLE_USER", authentication.getAuthorities().iterator().next().getAuthority());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_shouldNotReauthenticate_whenContextAlreadyHasAuthentication() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("already", null)
        );
        when(request.getHeader("Authorization")).thenReturn("Bearer valid-token");
        when(jwtService.isValid("valid-token")).thenReturn(true);

        filter.doFilterInternal(request, response, filterChain);

        verify(jwtService).isValid("valid-token");
        verify(jwtService, never()).extractEmail(anyString());
        verifyNoInteractions(authUserDetailsService);
        verify(filterChain).doFilter(request, response);
    }
}
