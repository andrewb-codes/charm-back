package ru.andrewb.charm.back.controller;

import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import ru.andrewb.charm.back.model.Gender;
import ru.andrewb.charm.back.model.Profile;
import ru.andrewb.charm.back.service.ProfileService;

import java.io.IOException;
import java.util.Optional;

@WebServlet(value = "/profile", loadOnStartup = 1)
public class ProfileController extends HttpServlet {

    private final ProfileService service = ProfileService.getInstance();

    @Override
    public void init(ServletConfig config) throws ServletException {
        ServletContext servletContext = config.getServletContext();
        if (servletContext.getAttribute("genders") == null) {
            servletContext.setAttribute("genders", Gender.values());
        }
    }

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
            if (id <= 0) throw new NumberFormatException("non-positive");
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

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String idParam = req.getParameter("id");

        Profile profile = new Profile();
        profile.setEmail(req.getParameter("email"));
        profile.setName(req.getParameter("name"));
        profile.setSurname(req.getParameter("surname"));
        profile.setAbout(req.getParameter("about"));
        profile.setGender(Gender.valueOf(req.getParameter("gender")));

        Long id = null;
        if (idParam != null && !idParam.isBlank()) {
            try {
                id = Long.parseLong(idParam);
                if (id <= 0) throw new NumberFormatException("non-positive");
            } catch (NumberFormatException e) {
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Param 'id' must be positive long");
                return;
            }
        }

        if (id == null) {
            id = service.save(profile).getId();
        } else {
            profile.setId(id);
            service.update(profile);
        }

        resp.sendRedirect(req.getContextPath() + "/profile?id=" + id);
    }
}
