package ru.andrewb.charm.back.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import ru.andrewb.charm.back.model.exception.BadRequestException;
import ru.andrewb.charm.back.model.exception.NotFoundException;
import ru.andrewb.charm.back.service.ContentService;

import java.io.IOException;
import java.io.InputStream;

import static ru.andrewb.charm.back.utils.UrlUtils.CONTENT_URL;

@WebServlet(CONTENT_URL + "/*")
public class ContentController extends HttpServlet {

    public static final ContentService contentService = ContentService.getInstance();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String prefix = req.getContextPath() + "/content";
        String contentPath = req.getRequestURI().substring(prefix.length());

        String fileName = contentPath.substring(contentPath.lastIndexOf('/') + 1);
        String mime = getServletContext().getMimeType(fileName);
        resp.setContentType(mime != null ? mime : "application/octet-stream");

        try {
            if (contentPath.startsWith("/app/")) {
                String appPath = "/WEB-INF" + contentPath.substring("/app".length());
                try (InputStream in = getServletContext().getResourceAsStream(appPath)) {
                    if (in == null) {
                        resp.sendError(HttpServletResponse.SC_NOT_FOUND, "resource not found");
                        return;
                    }
                    in.transferTo(resp.getOutputStream());
                }
            } else {
                contentService.download(resp.getOutputStream(), contentPath);
            }
        } catch (BadRequestException e) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } catch (NotFoundException e) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, e.getMessage());
        } catch (RuntimeException e) {
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "io error");
        }
    }
}
