package ru.andrewb.charm.back.controller.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import ru.andrewb.charm.back.service.bundle.WordBundle;
import ru.andrewb.charm.back.service.bundle.WordBundleEn;
import ru.andrewb.charm.back.service.bundle.WordBundleRu;

import java.io.IOException;
import java.util.Arrays;

@Component
public class LanguageFilter extends OncePerRequestFilter {

    private final WordBundleRu wordBundleRu;
    private final WordBundleEn wordBundleEn;

    public LanguageFilter(WordBundleRu wordBundleRu, WordBundleEn wordBundleEn) {
        this.wordBundleRu = wordBundleRu;
        this.wordBundleEn = wordBundleEn;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest req,
            HttpServletResponse resp,
            FilterChain filterChain
    ) throws IOException, ServletException {

        Cookie[] cookies = req.getCookies() == null ? new Cookie[]{} : req.getCookies();

        String lang = Arrays.stream(cookies)
                .filter(cookie -> "lang".equals(cookie.getName()))
                .map(Cookie::getValue)
                .findFirst()
                .orElse("en");

        WordBundle wordBundle = "ru".equals(lang) ? wordBundleRu : wordBundleEn;

        req.setAttribute("wordBundle", wordBundle);

        filterChain.doFilter(req, resp);
    }

    @Override
    protected boolean shouldNotFilterErrorDispatch() {
        return false;
    }
}
