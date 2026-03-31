package ru.andrewb.charm.back.web.flash;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.experimental.UtilityClass;

@UtilityClass
public class Flash {

    private static final String FLASH_SESSION_KEY = "FLASH_DATA";

    public static FlashData getOrCreate(HttpServletRequest req) {
        HttpSession s = req.getSession(true);
        FlashData data = (FlashData) s.getAttribute(FLASH_SESSION_KEY);
        if (data == null) {
            data = new FlashData();
            s.setAttribute(FLASH_SESSION_KEY, data);
        }
        return data;
    }

    public static void addError(HttpServletRequest req, String code) {
        getOrCreate(req).addError(code);
    }

    public static void putField(HttpServletRequest req, String name, String value) {
        getOrCreate(req).putField(name, value);
    }

    public static FlashData consume(HttpServletRequest req) {
        HttpSession s = req.getSession(false);
        if (s == null) return null;

        FlashData data = (FlashData) s.getAttribute(FLASH_SESSION_KEY);
        if (data != null) s.removeAttribute(FLASH_SESSION_KEY);

        return data;
    }
}
