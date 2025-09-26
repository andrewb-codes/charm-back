package ru.andrewb.charm.back.controller;

import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import ru.andrewb.charm.back.dto.ProfileGetDto;
import ru.andrewb.charm.back.mapper.ProfileSaveRequestMapper;
import ru.andrewb.charm.back.model.Gender;
import ru.andrewb.charm.back.service.ProfileService;

import java.io.IOException;
import java.util.Optional;

@WebServlet(value = "/profile", loadOnStartup = 1)
public class ProfileController extends HttpServlet {

    private final ProfileService service = ProfileService.getInstance();
    private final ProfileSaveRequestMapper saveRequestMapper = ProfileSaveRequestMapper.getInstance();

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
        if (idParam == null || idParam.isBlank()) {
            var profiles = service.findAll();
            req.setAttribute("profiles", profiles);
            req.getRequestDispatcher("/WEB-INF/jsp/profiles.jsp").forward(req, resp);
            return;
        }

        Long id = requirePositiveLong(req, resp);
        if (id == null) return;

        Optional<ProfileGetDto> profileGetDtoOptional = service.findById(id);
        if (profileGetDtoOptional.isEmpty()) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Profile not found");
            return;
        }
        req.setAttribute("profile", profileGetDtoOptional.get());
        req.getRequestDispatcher("/WEB-INF/jsp/profile.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        var dto = saveRequestMapper.map(req, resp);
        if (dto == null) return;
        long id = service.save(dto);
        resp.sendRedirect(req.getContextPath() + "/profile?id=" + id);
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Long id = requirePositiveLong(req, resp);
        if (id == null) return;
        if (service.findById(id).isEmpty()) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Profile not found");
            return;
        }
        var dto = saveRequestMapper.map(req, resp);
        if (dto == null) return;
        service.update(id, dto);
        resp.sendRedirect(req.getContextPath() + "/profile?id=" + id);
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Long id = requirePositiveLong(req, resp);
        if (id ==null) return;

        boolean removed = service.delete(id);
        if (!removed) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Profile not found");
            return;
        }
        resp.sendRedirect(req.getContextPath() + "/registration");
    }

    private Long requirePositiveLong(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String idParam = req.getParameter("id");
        if (idParam == null || idParam.isBlank()) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Param 'id' is required");
            return null;
        }
        long id;
        try {
            id = Long.parseLong(idParam);
            if (id <= 0) throw new NumberFormatException("non-positive");
            return id;
        } catch (NumberFormatException e) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Param 'id' must be positive long");
            return null;
        }
    }
}
