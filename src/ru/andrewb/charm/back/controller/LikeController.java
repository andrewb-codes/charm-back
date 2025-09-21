package ru.andrewb.charm.back.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import ru.andrewb.charm.back.service.LikeService;

import java.io.IOException;
import java.io.PrintWriter;

@WebServlet("/like")
public class LikeController extends HttpServlet {

    private final LikeService service = LikeService.getInstance();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String idParam = req.getParameter("id");
        long id;
        try {
            id = Long.parseLong(idParam);
            if (id <= 0) throw new NumberFormatException("negative");
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.setContentType("text/plain; charset=UTF-8");
            resp.getWriter().println("Bad request: query param id must be positive long");
            return;
        }

        resp.setContentType("text/html; charset=UTF-8");
        try (PrintWriter w = resp.getWriter()) {
            w.printf("<h2><p>Request URI: %s</p>", req.getRequestURI());
            w.printf("<p>User-Agent: %s</p>", req.getHeader("User-Agent"));
            w.printf("<p>Likes count: %d</p></h2>", service.getLikesById(id));
        }
    }
}
