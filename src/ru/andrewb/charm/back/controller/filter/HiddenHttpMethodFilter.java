package ru.andrewb.charm.back.controller.filter;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import ru.andrewb.charm.back.model.Gender;
import ru.andrewb.charm.back.model.Role;
import ru.andrewb.charm.back.model.Status;

import java.io.IOException;
import java.util.Locale;

@WebFilter(value = "/*", dispatcherTypes = DispatcherType.REQUEST)
public class HiddenHttpMethodFilter implements Filter {

    public static final String METHOD_PARAM = "_method";

    @Override
    public void init(FilterConfig config) {
        ServletContext servletContext = config.getServletContext();
        if (servletContext.getAttribute("genders") == null) {
            servletContext.setAttribute("genders", Gender.values());
        }
        if (servletContext.getAttribute("statuses") == null) {
            servletContext.setAttribute("statuses", Status.values());
        }
        if (servletContext.getAttribute("roles") == null) {
            servletContext.setAttribute("roles", Role.values());
        }
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) servletRequest;
        HttpServletResponse resp = (HttpServletResponse) servletResponse;

        String override = req.getParameter(METHOD_PARAM);
        if ("POST".equalsIgnoreCase(req.getMethod()) && override != null && !override.isBlank()) {
            String method = override.toUpperCase(Locale.ENGLISH);
            if (method.equals("PUT") || method.equals("DELETE")) {
                HttpServletRequest wrapped = new HttpMethodRequestWrapper(req, method);
                filterChain.doFilter(wrapped, resp);
                return;
            }
        }
        filterChain.doFilter(req, resp);
    }

    private static class HttpMethodRequestWrapper extends HttpServletRequestWrapper {

        private final String method;

        public HttpMethodRequestWrapper(HttpServletRequest request, String method) {
            super(request);
            this.method = method;
        }

        @Override
        public String getMethod() {
            return this.method;
        }
    }
}
