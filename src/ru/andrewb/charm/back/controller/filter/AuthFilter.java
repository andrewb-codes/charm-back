
package ru.andrewb.charm.back.controller.filter;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import ru.andrewb.charm.back.dto.UserDetailsDto;
import ru.andrewb.charm.back.model.Role;

import java.io.IOException;
import java.util.Set;

@WebFilter(value = "/*", dispatcherTypes = DispatcherType.REQUEST)
public class AuthFilter implements Filter {

    private static final Set<String> PUBLIC = Set.of(
            "/", "/login", "/registration", "/lang", "/content", "/logout"
    );

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) servletRequest;
        HttpServletResponse resp = (HttpServletResponse) servletResponse;

        String ctx = req.getContextPath();
        String path = req.getServletPath();
        if (path == null || path.isBlank()) path = "/";

        if (PUBLIC.contains(path)) {
            filterChain.doFilter(req, resp);
            return;
        }

        var userDetails = (UserDetailsDto) req.getSession().getAttribute("userDetails");
        if (userDetails == null) {
            resp.sendRedirect(ctx + "/login");
            return;
        }

        boolean isAdmin = userDetails.getRole() == Role.ADMIN;
        String requestId = req.getParameter("id");

        if ("/profile".equals(path) && (requestId == null || requestId.isBlank())) {
            if (isAdmin) {
                filterChain.doFilter(req, resp);
            } else {
                resp.sendRedirect(ctx + "/profile?id=" + userDetails.getId());
            }
            return;
        }

        boolean isSelf = requestId != null && requestId.equals(String.valueOf(userDetails.getId()));
        if (isAdmin || isSelf) {
            filterChain.doFilter(req, resp);
        } else {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN);
        }
    }
}
