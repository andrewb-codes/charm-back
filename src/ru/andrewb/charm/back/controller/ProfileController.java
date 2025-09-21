package ru.andrewb.charm.back.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import ru.andrewb.charm.back.model.Profile;
import ru.andrewb.charm.back.service.ProfileService;

import java.io.IOException;
import java.util.Optional;

@WebServlet("/profile")
public class ProfileController extends HttpServlet {

    private final ProfileService service = ProfileService.getInstance();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String idParam = req.getParameter("id");
        if (idParam == null) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Query param: 'id' is required");
            return;
        }

        long id;
        try {
            id = Long.parseLong(idParam);
            if (id <= 0) throw new NumberFormatException("negative");
        } catch (Exception e) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Param 'id' must be positive long");
            return;
        }

        Optional<Profile> profileOptional = service.findById(id);
        if (profileOptional.isEmpty()) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Profile not found");
            return;
        }
        req.setAttribute("profile", profileOptional.get());
        req.getRequestDispatcher("/WEB-INF/jsp/profile.jsp").forward(req, resp);
    }
}
