package ru.andrewb.charm.back.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.experimental.UtilityClass;
import ru.andrewb.charm.back.dto.UserDetailsDto;
import ru.andrewb.charm.back.model.Role;
import ru.andrewb.charm.back.web.RequestParamUtils;

@UtilityClass
public class AuthUtils {

    public record Ctx(UserDetailsDto user, long targetId, boolean isAdmin) {}

    public static Ctx getAuthCtx(HttpServletRequest req) {
        UserDetailsDto user = AuthUtils.getUserOrNull(req);
        if (user == null) return null;

        String idParam = req.getParameter("id");
        long id = (idParam == null || idParam.isBlank())
                ? user.getId()
                : RequestParamUtils.requirePositiveLong(req, "id");

        boolean isAdmin = (user.getRole() == Role.ADMIN);

        return new Ctx(user, id, isAdmin);
    }

    public static UserDetailsDto getUserOrNull(HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        return (session == null) ? null : (UserDetailsDto) session.getAttribute("userDetails");
    }

    public static boolean isAuthenticatedAdmin(HttpServletRequest req) {
        UserDetailsDto user = AuthUtils.getUserOrNull(req);
        return user != null && (user.getRole() == Role.ADMIN);
    }
}
