package ru.andrewb.charm.back.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import ru.andrewb.charm.back.dto.UserDetailsDto;
import ru.andrewb.charm.back.mapper.RequestToProfileUpdateDtoMapper;
import ru.andrewb.charm.back.model.exception.BadRequestException;
import ru.andrewb.charm.back.model.exception.NotFoundException;
import ru.andrewb.charm.back.service.ProfileService;
import ru.andrewb.charm.back.utils.RequestParams;
import ru.andrewb.charm.back.validator.ProfileUpdateValidator;
import ru.andrewb.charm.back.web.flash.Flash;

import java.io.IOException;

import static ru.andrewb.charm.back.utils.RequestParams.rid;

@WebServlet("/profile")
@Slf4j
@MultipartConfig
public class ProfileController extends HttpServlet {

    private final ProfileService service = ProfileService.getInstance();
    private final ProfileUpdateValidator profileUpdateValidator = ProfileUpdateValidator.getInstance();
    private final RequestToProfileUpdateDtoMapper requestToProfileUpdateDtoMapper = RequestToProfileUpdateDtoMapper.getInstance();

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

            var flash = Flash.consume(req);
            if (flash != null) {
                if (!flash.getErrors().isEmpty()) {
                    req.setAttribute("errors", flash.getErrors());
                }
                if (!flash.getFields().isEmpty()) {
                    req.setAttribute("fields", flash.getFields());
                }
            }

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

            boolean fromList = "list".equals(req.getParameter("from"));
            String redirectUrl = fromList
                    ? req.getContextPath() + "/profile"
                    : req.getContextPath() + "/profile?id=" + id;


            var vr = profileUpdateValidator.validate(dto);
            if (vr.isNotValid()) {
                vr.getErrors().forEach(code -> Flash.addError(req, code));
                if (!fromList) {
                    Flash.putField(req, "name", dto.getName());
                    Flash.putField(req, "surname", dto.getSurname());
                    Flash.putField(req, "about", dto.getAbout());
                }
                resp.sendRedirect(redirectUrl);
                return;
            }

            service.update(id, dto);
            log.info("[{}] Profile updated: id={}", rid(req), id);
            resp.sendRedirect(redirectUrl);

        } catch (NotFoundException e) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, e.getMessage());
        } catch (BadRequestException e) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        long id;
        try {
            id = RequestParams.requirePositiveLong(req, "id");
        } catch (BadRequestException e) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
            return;
        }

        boolean deleted = service.delete(id);
        var sessionUser = (UserDetailsDto) req.getSession().getAttribute("userDetails");

        if (deleted) {
            log.info("[{}] Profile deleted: id={}", rid(req), id);

            if (sessionUser != null && sessionUser.getId() != null && sessionUser.getId().equals(id)) {
                req.getSession().invalidate();
                resp.sendRedirect(req.getContextPath() + "/login");
                return;
            }
        } else {
            log.warn("[{}] Delete ignored (not found): id={}", rid(req), id);
        }

        resp.sendRedirect(req.getContextPath() + "/profile");

    }
}
