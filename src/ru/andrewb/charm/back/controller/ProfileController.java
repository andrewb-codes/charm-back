package ru.andrewb.charm.back.controller;

import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import ru.andrewb.charm.back.mapper.RequestToProfileUpdateDtoMapper;
import ru.andrewb.charm.back.model.Gender;
import ru.andrewb.charm.back.model.Status;
import ru.andrewb.charm.back.model.exception.BadRequestException;
import ru.andrewb.charm.back.model.exception.DuplicateEmailException;
import ru.andrewb.charm.back.model.exception.NotFoundException;
import ru.andrewb.charm.back.service.ProfileService;
import ru.andrewb.charm.back.utils.RequestParams;

import java.io.IOException;

@WebServlet(value = "/profile", loadOnStartup = 1)
@Slf4j
public class ProfileController extends HttpServlet {

    private final ProfileService service = ProfileService.getInstance();
    private final RequestToProfileUpdateDtoMapper requestToProfileUpdateDtoMapper = RequestToProfileUpdateDtoMapper.getInstance();

    @Override
    public void init(ServletConfig config) throws ServletException {
        ServletContext servletContext = config.getServletContext();
        if (servletContext.getAttribute("genders") == null) {
            servletContext.setAttribute("genders", Gender.values());
        }
        if (servletContext.getAttribute("statuses") == null) {
            servletContext.setAttribute("statuses", Status.values());
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

        try {
            long id = RequestParams.requirePositiveLong(req, "id");
            var dto = service.findByIdOrThrow(id);
            req.setAttribute("profile", dto);
            req.getRequestDispatcher("/WEB-INF/jsp/profile.jsp").forward(req, resp);
        } catch (BadRequestException e) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } catch (NotFoundException e) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, e.getMessage());
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            long id = RequestParams.requirePositiveLong(req, "id");
            var dto = requestToProfileUpdateDtoMapper.map(req);

            service.update(id, dto);
            log.info("[{}] Profile updated: id={}", rid(req), id);

            resp.sendRedirect(req.getContextPath() + "/profile?id=" + id);
        } catch (NotFoundException e) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, e.getMessage());
        } catch (BadRequestException e) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } catch (DuplicateEmailException e) {
            resp.sendError(HttpServletResponse.SC_CONFLICT, e.getMessage());
        }
    }

    private static String rid(HttpServletRequest req) {
        Object v = req.getAttribute("rid");
        return v == null ? "-" : v.toString();
    }
}
