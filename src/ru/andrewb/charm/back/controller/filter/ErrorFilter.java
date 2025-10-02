package ru.andrewb.charm.back.controller.filter;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import static jakarta.servlet.RequestDispatcher.*;

@WebFilter(value = "/*", dispatcherTypes = DispatcherType.ERROR)
public class ErrorFilter implements Filter {

    private static final Logger log = LoggerFactory.getLogger(ErrorFilter.class);

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) servletRequest;

        Integer code = (Integer) req.getAttribute(ERROR_STATUS_CODE);
        Throwable throwable = (Throwable) req.getAttribute(ERROR_EXCEPTION);
        String uri = (String) req.getAttribute(ERROR_REQUEST_URI);
        String msg = (String) req.getAttribute(ERROR_MESSAGE);
        String rid = (String) req.getAttribute("rid");

        if (code != null && code >= 500) {
            if (throwable != null) {
                log.error("[{}] {} at {}: {}", rid, code, uri, msg, throwable);
            } else {
                log.error("[{}] {} at {}: {}", rid, code, uri, msg);
            }
        } else {
            log.warn("[{}] {} at {}: {}", rid, code, uri, msg);
        }

        filterChain.doFilter(servletRequest, servletResponse);
    }
}
