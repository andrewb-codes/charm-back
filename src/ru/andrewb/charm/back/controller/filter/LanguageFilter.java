
package ru.andrewb.charm.back.controller.filter;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import ru.andrewb.charm.back.service.WordBundle;

import java.io.IOException;
import java.util.Arrays;

@WebFilter("/*")
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

        WordBundle wordBundle = new WordBundle(lang);

        req.setAttribute("wordBundle", wordBundle);

        filterChain.doFilter(req, resp);
    }
}
