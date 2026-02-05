
package ru.andrewb.charm.back.controller.filter;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import ru.andrewb.charm.back.dto.UserDetailsDto;
import ru.andrewb.charm.back.model.Role;
import ru.andrewb.charm.back.security.AuthUtils;

import java.io.IOException;

import static ru.andrewb.charm.back.security.SecurityRules.*;
import static ru.andrewb.charm.back.web.Urls.*;


@WebFilter(value = "/*", dispatcherTypes = DispatcherType.REQUEST)
public class AuthFilter implements Filter {

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) servletRequest;
        HttpServletResponse resp = (HttpServletResponse) servletResponse;

        String ctx = req.getContextPath();
        String path = req.getServletPath();
        if (path == null || path.isBlank()) path = "/";

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
            // Other rest (only for admin)
            if (user == null) {
                resp.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }
            if (!isAdmin) {
                resp.sendError(HttpServletResponse.SC_FORBIDDEN);
                return;
            }

            filterChain.doFilter(req, resp);
            return;
        }

        // === UI ===
        if (PUBLIC_UI.contains(path)) {
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
