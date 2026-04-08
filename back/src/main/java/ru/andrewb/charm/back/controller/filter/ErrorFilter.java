package ru.andrewb.charm.back.controller.filter;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import ru.andrewb.charm.back.mapper.JsonMapper;
import ru.andrewb.charm.back.service.bundle.WordBundle;
import ru.andrewb.charm.back.service.bundle.WordBundleEn;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static jakarta.servlet.RequestDispatcher.*;
import static jakarta.servlet.http.HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
import static ru.andrewb.charm.back.security.SecurityRules.isRest;

@Slf4j
//@WebFilter(value = "/*", dispatcherTypes = DispatcherType.ERROR)
public class ErrorFilter implements Filter {

    private final JsonMapper jsonMapper = JsonMapper.getInstance();

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) servletRequest;
        HttpServletResponse resp = (HttpServletResponse) servletResponse;

        Integer code = (Integer) req.getAttribute(ERROR_STATUS_CODE);
        Throwable throwable = (Throwable) req.getAttribute(ERROR_EXCEPTION);
        String uri = (String) req.getAttribute(ERROR_REQUEST_URI);
        String msg = (String) req.getAttribute(ERROR_MESSAGE);
        String rid = (String) req.getAttribute("rid");

        if (code != null && code >= SC_INTERNAL_SERVER_ERROR) {
            if (throwable != null) {
                log.error("[{}] {} at {}: {}", rid, code, uri, msg, throwable);
            } else {
                log.error("[{}] {} at {}: {}", rid, code, uri, msg);
            }
        } else {
            log.warn("[{}] {} at {}: {}", rid, code, uri, msg);
        }

        String ctx = req.getContextPath();
        String path = uri == null ? "" : uri;
        if (ctx != null && !ctx.isBlank() && path.startsWith(ctx)) {
            path = path.substring(ctx.length());
        }

        if (isRest(path)) {
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("status", "error");
            if (code != null) body.put("code", code);

            WordBundle wb = (WordBundle) req.getAttribute("wordBundle");
            if (wb == null) wb = WordBundleEn.getInstance();

            Object errorsAttr = req.getAttribute("errors");
            if (errorsAttr instanceof List<?> list && !list.isEmpty()) {
                List<String> errors = list.stream()
                        .map(String::valueOf)
                        .map(wb::getWord)
                        .toList();
                body.put("errors", errors);
            } else {
                String outMsg = null;
                if (msg != null && !msg.isBlank()) {
                    if (msg.startsWith("error.")) {
                        outMsg = wb.getWord(msg);
                    } else {
                        if (code != null && code < 500) outMsg = msg;
                    }
                }

                if (outMsg == null) {
                    String key = (code != null && code >= 500) ? "error.internal" : "error.param.invalid";
                    outMsg = wb.getWord(key);
                }

                body.put("message", outMsg);
            }

            if (code != null) resp.setStatus(code);
            resp.setContentType("application/json;charset=UTF-8");
            jsonMapper.writeValue(resp.getWriter(), body);
            return;
        }

        filterChain.doFilter(req, resp);
    }
}
