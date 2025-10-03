package ru.andrewb.charm.back.controller.filter;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;

import java.io.IOException;
import java.util.UUID;

@WebFilter(value = "/*", dispatcherTypes = DispatcherType.REQUEST)
@Slf4j
public class RequestLoggingFilter implements Filter {
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) servletRequest;
        HttpServletResponse resp = (HttpServletResponse) servletResponse;

        long start = System.currentTimeMillis();
        String rid = UUID.randomUUID().toString().substring(0, 8);

        req.setAttribute("rid", rid);
        MDC.put("rid", rid);

        String uri = req.getRequestURI();
        String qs = req.getQueryString();
        String method = req.getMethod();

        log.info("[{}] -> {} {}{}", rid, method, uri, (qs == null ? "" : "?" + qs));
        try {
            filterChain.doFilter(req, resp);
        } finally {
            long took = System.currentTimeMillis() - start;
            int status = resp.getStatus();
            log.info("[{}] <- {} {} ({} ms)", rid, status, uri, took);
            MDC.clear();
        }
    }
}
