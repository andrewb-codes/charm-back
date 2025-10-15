package ru.andrewb.charm.back.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

import static ru.andrewb.charm.back.utils.UrlUtils.LANG_URL;

@WebServlet(LANG_URL)
public class LanguageController extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String lang = req.getParameter("lang");
        String value = "en";
        if ("ru".equals(lang)) value = "ru";

        Cookie cookie = new Cookie("lang", value);

        String ctx = req.getContextPath();
        cookie.setPath((ctx == null || ctx.isBlank()) ? "/" : ctx);

        cookie.setMaxAge(60 * 60 * 24 * 365);

        cookie.setHttpOnly(true);
        cookie.setSecure(req.isSecure());

        resp.addCookie(cookie);

        String back = req.getHeader("referer");
        if (back == null || back.isBlank()) back = req.getContextPath() + "/";
        resp.sendRedirect(back);
    }
}
