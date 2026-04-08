package ru.andrewb.charm.back.controller.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Slf4j
@Component
public class RequestLoggingFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(
            HttpServletRequest req,
            HttpServletResponse resp,
            FilterChain filterChain
    ) throws IOException, ServletException {

        String rid = UUID.randomUUID().toString().substring(0, 8);

        req.setAttribute("rid", rid);
        MDC.put("rid", rid);

        String uri = req.getRequestURI();
        String qs = req.getQueryString();
        String method = req.getMethod();

        log.info("[{}] -> {} {}{}", rid, method, uri, (qs == null ? "" : "?" + qs));

        long start = System.currentTimeMillis();
        int status = 200;
        try {
            filterChain.doFilter(req, resp);
            status = resp.getStatus();
        } catch (Throwable t) {
            status = 500;
            throw t;
        } finally {
            long took = System.currentTimeMillis() - start;
            MDC.clear();
        }
    }
}
