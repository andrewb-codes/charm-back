package ru.andrewb.charm.back.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.experimental.UtilityClass;
import ru.andrewb.charm.back.dto.UserDetailsDto;

@UtilityClass
public class AuthUtils {

    public static UserDetailsDto getUserOrNull(HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        return (session == null) ? null : (UserDetailsDto) session.getAttribute("userDetails");
    }
}
