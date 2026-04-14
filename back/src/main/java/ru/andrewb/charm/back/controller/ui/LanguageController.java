package ru.andrewb.charm.back.controller.ui;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;

import static ru.andrewb.charm.back.web.Urls.LANG_URL;

@Controller
@RequestMapping(LANG_URL)
public class LanguageController {

    @PostMapping
    public void changeLanguage(
            @RequestParam(name = "lang", required = false) String lang,
            HttpServletRequest req,
            HttpServletResponse resp
    ) throws IOException {
        String value = "ru".equals(lang) ? "ru" : "en";

        Cookie cookie = new Cookie("lang", value);

        String ctx = req.getContextPath();
        cookie.setPath((ctx == null || ctx.isBlank()) ? "/" : ctx);
        cookie.setMaxAge(60 * 60 * 24 * 365);
        cookie.setHttpOnly(true);
        cookie.setSecure(req.isSecure());

        resp.addCookie(cookie);

        String back = req.getHeader("referer");
        if (back == null || back.isBlank()) {
            back = req.getContextPath() + "/";
        }
        resp.sendRedirect(back);
    }
}
