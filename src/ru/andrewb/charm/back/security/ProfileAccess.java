package ru.andrewb.charm.back.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.experimental.UtilityClass;
import ru.andrewb.charm.back.dto.UserDetailsDto;
import ru.andrewb.charm.back.model.Role;
import ru.andrewb.charm.back.web.RequestParamUtils;

import java.io.IOException;

import static ru.andrewb.charm.back.web.Urls.LOGIN_URL;

@UtilityClass
public class ProfileAccess {

    public record Ctx(UserDetailsDto user, long targetId, boolean isAdmin) {}

    public static Ctx resolveOrSend(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        UserDetailsDto user = AuthUtils.getUserOrNull(req);
        if (user == null) {
            resp.sendRedirect(req.getContextPath() + LOGIN_URL);
            return null;
        }

        String idParam = req.getParameter("id");
        long id = (idParam == null || idParam.isBlank())
                ? user.getId()
                : RequestParamUtils.requirePositiveLong(req, "id");

        boolean isAdmin = (user.getRole() == Role.ADMIN);
        if (!isAdmin && id != user.getId()) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN);
            return null;
        }

        return new Ctx(user, id, isAdmin);
    }

    public static Ctx resolveOrSendRest(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        UserDetailsDto user = AuthUtils.getUserOrNull(req);
        if (user == null) {
            resp.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return null;
        }

        String idParam = req.getParameter("id");
        long id = (idParam == null || idParam.isBlank())
                ? user.getId()
                : RequestParamUtils.requirePositiveLong(req, "id");

        boolean isAdmin = (user.getRole() == Role.ADMIN);
        if (!isAdmin && id != user.getId()) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN);
            return null;
        }

        return new Ctx(user, id, isAdmin);
    }

}
