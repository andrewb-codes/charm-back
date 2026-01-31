
package ru.andrewb.charm.back.controller.filter;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import ru.andrewb.charm.back.dto.UserDetailsDto;
import ru.andrewb.charm.back.model.Role;

import java.io.IOException;

import static ru.andrewb.charm.back.security.SecurityRules.*;
import static ru.andrewb.charm.back.utils.Urls.LOGIN_URL;
import static ru.andrewb.charm.back.utils.Urls.PROFILE_URL;


@WebFilter(value = "/*", dispatcherTypes = DispatcherType.REQUEST)
public class AuthFilter implements Filter {

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) servletRequest;
        HttpServletResponse resp = (HttpServletResponse) servletResponse;

        String ctx = req.getContextPath();
        String path = req.getServletPath();
        if (path == null || path.isBlank()) path = "/";

        boolean rest = isRest(path);
        UserDetailsDto user = (UserDetailsDto) req.getSession().getAttribute("userDetails");
        boolean authenticated = (user != null);
        boolean isAdmin = authenticated && user.getRole() == Role.ADMIN;


        // === REST ===
        if (rest) {
            // Public rest
            if (PUBLIC_REST.contains(path)) {
                filterChain.doFilter(req, resp);
                return;
            }
            // Other rest (only for admin)
            if (!authenticated) {
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

        if (!authenticated) {
            resp.sendRedirect(ctx + LOGIN_URL);
            return;
        }

        // "/profile" (no "id" param)
        String requestId = req.getParameter("id");
        if (PROFILE_URL.equals(path)) {
            if (requestId == null || requestId.isBlank()) {
                if (isAdmin) {
                    filterChain.doFilter(req, resp); // Admin can go to profiles list
                } else {
                    // Redirect user to his profile
                    resp.sendRedirect(ctx + PROFILE_URL + "?id=" + user.getId());
                }
                return;
            }
        }

        // check self or admin only if "id" in request
        if (requestId != null && !requestId.isBlank()) {
            boolean isSelf = String.valueOf(user.getId()).equals(requestId);
            if (!isAdmin && !isSelf) {
                resp.sendError(HttpServletResponse.SC_FORBIDDEN);
                return;
            }
        }
        // otherwise (no "id" in request) - controller will decide
        filterChain.doFilter(req, resp);
    }
}
