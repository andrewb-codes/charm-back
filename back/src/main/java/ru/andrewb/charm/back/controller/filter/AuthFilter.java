package ru.andrewb.charm.back.controller.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import ru.andrewb.charm.back.dto.UserDetailsDto;
import ru.andrewb.charm.back.model.Role;
import ru.andrewb.charm.back.security.AuthUtils;

import java.io.IOException;

import static ru.andrewb.charm.back.security.SecurityRules.*;
import static ru.andrewb.charm.back.web.Urls.*;

@Slf4j
@Component
public class AuthFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(
            HttpServletRequest req,
            HttpServletResponse resp,
            FilterChain filterChain
    ) throws IOException, ServletException {

        String ctx = req.getContextPath();
        String path = req.getRequestURI().substring(ctx.length());
        if (path.isEmpty()) path = "/";

        UserDetailsDto user = AuthUtils.getUserOrNull(req);
        boolean isAdmin = (user != null && user.getRole() == Role.ADMIN);
        boolean rest = isRest(path);

        // === REST ===
        if (rest) {
            // Public rest
            if (PUBLIC_REST.contains(path)) {
                filterChain.doFilter(req, resp);
                return;
            }
            // Everything else requires login
            if (user == null) {
                resp.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }
            // Profiles list only for admin
            if (PROFILES_REST_URL.equals(path) && !isAdmin) {
                resp.sendError(HttpServletResponse.SC_FORBIDDEN);
                return;
            }

            filterChain.doFilter(req, resp);
            return;
        }

        // === UI / STATIC ===
        // Public UI + static (img/css/js/...) always allow
        if (isPublicUi(path)) {
            filterChain.doFilter(req, resp);
            return;
        }
        // Everything else requires login
        if (user == null) {
            resp.sendRedirect(ctx + LOGIN_URL);
            return;
        }
        // Profiles list only for admin
        if (PROFILES_URL.equals(path) && !isAdmin) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        filterChain.doFilter(req, resp);
    }
}
