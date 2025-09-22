package ru.andrewb.charm.back.controller.filter;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Locale;

@WebFilter("/*")
public class HiddenHttpMethodFilter implements Filter {

    public static final String METHOD_PARAM = "_method";

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        String paramValue = request.getParameter(METHOD_PARAM);

        if ("POST".equals(request.getMethod()) && paramValue != null && !paramValue.isBlank()) {
            String method = paramValue.toUpperCase(Locale.ENGLISH);
            HttpServletRequest wrapper = new HttpMethodRequestWrapper(request, method);
            filterChain.doFilter(wrapper, response);
        } else {
            filterChain.doFilter(request, response);
        }
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
