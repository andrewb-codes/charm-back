package ru.andrewb.charm.back.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import ru.andrewb.charm.back.dto.UserDetailsDto;
import ru.andrewb.charm.back.model.exception.BadRequestException;
import ru.andrewb.charm.back.model.exception.NotFoundException;
import ru.andrewb.charm.back.security.AuthUtils;
import ru.andrewb.charm.back.service.ProfileService;
import ru.andrewb.charm.back.web.flash.Flash;

import java.io.IOException;

import static ru.andrewb.charm.back.web.Urls.LOGIN_URL;
import static ru.andrewb.charm.back.web.Urls.SETTINGS_URL;
import static ru.andrewb.charm.back.web.Views.getJspPath;

@WebServlet(SETTINGS_URL)
public class SettingsController extends HttpServlet {

    private final ProfileService service = ProfileService.getInstance();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            UserDetailsDto user = AuthUtils.getUserOrNull(req);
            if (user == null) {
                resp.sendRedirect(req.getContextPath() + LOGIN_URL);
                return;
            }

            long id = user.getId();
            var dto = service.findByIdOrThrow(id);
            req.setAttribute("profile", dto);

            var flash = Flash.consume(req);
            if (flash != null) {
                if (!flash.getErrors().isEmpty()) {
                    req.setAttribute("errors", flash.getErrors());
                }
                if (!flash.getFields().isEmpty()) {
                    req.setAttribute("fields", flash.getFields());
                }
            }

            req.getRequestDispatcher(getJspPath(SETTINGS_URL)).forward(req, resp);

        } catch (BadRequestException e) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } catch (NotFoundException e) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, e.getMessage());
        }
    }
}
