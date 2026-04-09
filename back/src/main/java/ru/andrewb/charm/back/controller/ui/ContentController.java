package ru.andrewb.charm.back.controller.ui;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import ru.andrewb.charm.back.model.Role;
import ru.andrewb.charm.back.model.exception.BadRequestException;
import ru.andrewb.charm.back.model.exception.NotFoundException;
import ru.andrewb.charm.back.security.AuthUtils;
import ru.andrewb.charm.back.service.ContentService;

import java.io.IOException;

import static ru.andrewb.charm.back.web.Urls.CONTENT_URL;
import static ru.andrewb.charm.back.web.Urls.LOGIN_URL;

@Controller
@RequestMapping(CONTENT_URL)
public class ContentController {

    public final ContentService service;

    public ContentController(ContentService service) {
        this.service = service;
    }

    @GetMapping("/**")
    public void downloadContent(
            HttpServletRequest req,
            HttpServletResponse resp
    ) throws IOException {
        var user = AuthUtils.getUserOrNull(req);
        if (user == null) {
            resp.sendRedirect(req.getContextPath() + LOGIN_URL);
            return;
        }

        String prefix = req.getContextPath() + CONTENT_URL;
        String contentPath = req.getRequestURI().substring(prefix.length());
        if (contentPath.isBlank()) {
            contentPath = "/";
        }

        String[] parts = contentPath.split("/");
        if (parts.length < 4 || !"profile".equals(parts[1])) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "error.content.bad-path");
            return;
        }

        long requestedUserId;
        try {
            requestedUserId = Long.parseLong(parts[2]);
        } catch (NumberFormatException e) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "error.content.bad-user-id");
            return;
        }

        boolean isAdmin = user.getRole() == Role.ADMIN;
        boolean isOwner = user.getId() == requestedUserId;
        if (!isOwner && !isAdmin) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        String fileName = contentPath.substring(contentPath.lastIndexOf('/') + 1);
        String mime = req.getServletContext().getMimeType(fileName);
        resp.setContentType(mime != null ? mime : "application/octet-stream");

        try {
            service.download(resp.getOutputStream(), contentPath);

        } catch (BadRequestException e) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());

        } catch (NotFoundException e) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, e.getMessage());

        } catch (RuntimeException e) {
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "error.internal");
        }
    }
}
