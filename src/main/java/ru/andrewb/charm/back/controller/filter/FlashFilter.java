package ru.andrewb.charm.back.controller.filter;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import ru.andrewb.charm.back.web.flash.FlashData;

import java.io.IOException;

@WebFilter(value = "/*", dispatcherTypes = DispatcherType.REQUEST)
public class FlashFilter implements Filter {
    private static final String FLASH_SESSION_KEY = "FLASH_DATA";

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) servletRequest;
        HttpSession s = req.getSession(false);
        if (s != null) {
            Object o = s.getAttribute(FLASH_SESSION_KEY);
            if (o instanceof FlashData data) {
                req.setAttribute(FLASH_SESSION_KEY, data);
            }
            s.removeAttribute(FLASH_SESSION_KEY);
        }
        filterChain.doFilter(req, servletResponse);
    }
}
