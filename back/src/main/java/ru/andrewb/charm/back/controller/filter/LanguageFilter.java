package ru.andrewb.charm.back.controller.filter;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import ru.andrewb.charm.back.service.bundle.WordBundle;
import ru.andrewb.charm.back.service.bundle.WordBundleEn;
import ru.andrewb.charm.back.service.bundle.WordBundleRu;

import java.io.IOException;
import java.util.Arrays;

@WebFilter(value = "/*", dispatcherTypes = {DispatcherType.REQUEST, DispatcherType.ERROR})
public class LanguageFilter implements Filter {

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) servletRequest;
        HttpServletResponse resp = (HttpServletResponse) servletResponse;

        Cookie[] cookies = req.getCookies() == null ? new Cookie[]{} : req.getCookies();

        String lang = Arrays.stream(cookies)
                .filter(cookie -> "lang".equals(cookie.getName()))
                .map(Cookie::getValue)
                .findFirst()
                .orElse("en");

        WordBundle wordBundle = "ru".equals(lang)
                        ? WordBundleRu.getInstance()
                        : WordBundleEn.getInstance();

        req.setAttribute("wordBundle", wordBundle);

        filterChain.doFilter(req, resp);
    }
}
