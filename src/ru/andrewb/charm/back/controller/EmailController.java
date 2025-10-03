package ru.andrewb.charm.back.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.andrewb.charm.back.mapper.RequestToProfileUpdateDtoMapper;
import ru.andrewb.charm.back.model.exception.BadRequestException;
import ru.andrewb.charm.back.model.exception.DuplicateEmailException;
import ru.andrewb.charm.back.model.exception.NotFoundException;
import ru.andrewb.charm.back.service.ProfileService;
import ru.andrewb.charm.back.utils.RequestParams;

import java.io.IOException;

@WebServlet("/email")
public class EmailController extends HttpServlet {

    private static final Logger log = LoggerFactory.getLogger(EmailController.class);

    private final ProfileService service = ProfileService.getInstance();

    private final RequestToProfileUpdateDtoMapper requestToProfileUpdateDtoMapper = RequestToProfileUpdateDtoMapper.getInstance();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            long id = RequestParams.requirePositiveLong(req, "id");
            var dto = service.findByIdOrThrow(id);
            req.setAttribute("profile", dto);
            req.getRequestDispatcher("/WEB-INF/jsp/email.jsp").forward(req, resp);
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
            log.info("[{}] Email changed: id={}", rid(req), id);

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
