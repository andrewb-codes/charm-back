package ru.andrewb.charm.back.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.Test;
import ru.andrewb.charm.back.dto.UserDetailsDto;
import ru.andrewb.charm.back.model.Role;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AuthUtilsTest {

    @Test
    void getUserOrNull_shouldReturnNull_whenSessionDoesNotExist() {
        HttpServletRequest req = mock(HttpServletRequest.class);
        when(req.getSession(false)).thenReturn(null);

        UserDetailsDto result = AuthUtils.getUserOrNull(req);

        assertNull(result);
    }

    @Test
    void getUserOrNull_shouldReturnUser_whenSessionContainsUserDetails() {
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpSession session = mock(HttpSession.class);

        UserDetailsDto user = new UserDetailsDto();
        user.setId(1L);
        user.setEmail("user@mail.com");
        user.setRole(Role.USER);

        when(req.getSession(false)).thenReturn(session);
        when(session.getAttribute("userDetails")).thenReturn(user);

        UserDetailsDto result = AuthUtils.getUserOrNull(req);

        assertSame(user, result);
    }

    @Test
    void getAuthCtx_shouldReturnNull_whenUserIsNotAuthenticated() {
        HttpServletRequest req = mock(HttpServletRequest.class);
        when(req.getSession(false)).thenReturn(null);

        AuthUtils.Ctx result = AuthUtils.getAuthCtx(req);

        assertNull(result);
    }

    @Test
    void getAuthCtx_shouldUseCurrentUserId_whenIdParamIsMissing() {
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpSession session = mock(HttpSession.class);

        UserDetailsDto user = new UserDetailsDto();
        user.setId(15L);
        user.setRole(Role.USER);

        when(req.getSession(false)).thenReturn(session);
        when(session.getAttribute("userDetails")).thenReturn(user);
        when(req.getParameter("id")).thenReturn(null);

        AuthUtils.Ctx ctx = AuthUtils.getAuthCtx(req);

        assertNotNull(ctx);
        assertSame(user, ctx.user());
        assertEquals(15L, ctx.targetId());
        assertFalse(ctx.isAdmin());
    }

    @Test
    void getAuthCtx_shouldUseCurrentUserId_whenIdParamIsBlank() {
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpSession session = mock(HttpSession.class);

        UserDetailsDto user = new UserDetailsDto();
        user.setId(20L);
        user.setRole(Role.USER);

        when(req.getSession(false)).thenReturn(session);
        when(session.getAttribute("userDetails")).thenReturn(user);
        when(req.getParameter("id")).thenReturn("   ");

        AuthUtils.Ctx ctx = AuthUtils.getAuthCtx(req);

        assertNotNull(ctx);
        assertEquals(20L, ctx.targetId());
        assertFalse(ctx.isAdmin());
    }

    @Test
    void getAuthCtx_shouldUseRequestedId_whenUserIsAdminAndIdParamIsPresent() {
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpSession session = mock(HttpSession.class);

        UserDetailsDto admin = new UserDetailsDto();
        admin.setId(1L);
        admin.setRole(Role.ADMIN);

        when(req.getSession(false)).thenReturn(session);
        when(session.getAttribute("userDetails")).thenReturn(admin);
        when(req.getParameter("id")).thenReturn("42");

        AuthUtils.Ctx ctx = AuthUtils.getAuthCtx(req);

        assertNotNull(ctx);
        assertSame(admin, ctx.user());
        assertEquals(42L, ctx.targetId());
        assertTrue(ctx.isAdmin());
    }

    @Test
    void isAuthenticatedAdmin_shouldReturnTrue_whenUserIsAdmin() {
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpSession session = mock(HttpSession.class);

        UserDetailsDto admin = new UserDetailsDto();
        admin.setId(1L);
        admin.setRole(Role.ADMIN);

        when(req.getSession(false)).thenReturn(session);
        when(session.getAttribute("userDetails")).thenReturn(admin);

        assertTrue(AuthUtils.isAuthenticatedAdmin(req));
    }

    @Test
    void isAuthenticatedAdmin_shouldReturnFalse_whenUserIsNotAdmin() {
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpSession session = mock(HttpSession.class);

        UserDetailsDto user = new UserDetailsDto();
        user.setId(2L);
        user.setRole(Role.USER);

        when(req.getSession(false)).thenReturn(session);
        when(session.getAttribute("userDetails")).thenReturn(user);

        assertFalse(AuthUtils.isAuthenticatedAdmin(req));
    }

    @Test
    void isAuthenticatedAdmin_shouldReturnFalse_whenUserIsMissing() {
        HttpServletRequest req = mock(HttpServletRequest.class);
        when(req.getSession(false)).thenReturn(null);

        assertFalse(AuthUtils.isAuthenticatedAdmin(req));
    }
}
