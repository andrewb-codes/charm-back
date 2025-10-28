package ru.andrewb.charm.back.controller;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.PdfWriter;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import ru.andrewb.charm.back.dto.UserDetailsDto;
import ru.andrewb.charm.back.mapper.ProfileGetDtoToPdfMapper;
import ru.andrewb.charm.back.mapper.RequestToProfileUpdateDtoMapper;
import ru.andrewb.charm.back.model.exception.BadRequestException;
import ru.andrewb.charm.back.model.exception.NotFoundException;
import ru.andrewb.charm.back.security.SecurityRules;
import ru.andrewb.charm.back.service.ProfileService;
import ru.andrewb.charm.back.utils.RequestParams;
import ru.andrewb.charm.back.validator.ProfileUpdateValidator;
import ru.andrewb.charm.back.web.flash.Flash;

import java.io.IOException;
import java.io.OutputStream;

import static ru.andrewb.charm.back.utils.RequestParams.rid;
import static ru.andrewb.charm.back.utils.UrlUtils.*;

@WebServlet(PROFILE_URL + "/*")
@Slf4j
@MultipartConfig
public class ProfileController extends HttpServlet {

    private final ProfileService service = ProfileService.getInstance();
    private final ProfileUpdateValidator profileUpdateValidator = ProfileUpdateValidator.getInstance();
    private final RequestToProfileUpdateDtoMapper requestToProfileUpdateDtoMapper = RequestToProfileUpdateDtoMapper.getInstance();
    private final ProfileGetDtoToPdfMapper profileGetDtoToPdfMapper = ProfileGetDtoToPdfMapper.getInstance();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            long id = RequestParams.requirePositiveLong(req, "id");
            var dto = service.findByIdOrThrow(id);
            req.setAttribute("profile", dto);

            String pathInfo = req.getPathInfo();
            if ("/pdf".equals(pathInfo)) {
                resp.setHeader("Content-Disposition", "attachment; filename=\"profile-" + id + ".pdf\"");
                resp.setContentType("application/pdf");

                try (OutputStream out = resp.getOutputStream()) {
                    Document pdf = new Document();
                    PdfWriter.getInstance(pdf, out);
                    profileGetDtoToPdfMapper.map(dto, pdf);
                } catch (DocumentException e) {
                    throw new IOException(e);
                }

                log.info("[{}] PDF downloaded: id={}", rid(req), id);
                return;
            }

            var flash = Flash.consume(req);
            if (flash != null) {
                if (!flash.getErrors().isEmpty()) {
                    req.setAttribute("errors", flash.getErrors());
                }
                if (!flash.getFields().isEmpty()) {
                    req.setAttribute("fields", flash.getFields());
                }
            }

            req.getRequestDispatcher(getJspPath("/profile")).forward(req, resp);

        } catch (BadRequestException e) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } catch (NotFoundException e) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, e.getMessage());
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            long id = RequestParams.requirePositiveLong(req, "id");
            var dto = requestToProfileUpdateDtoMapper.map(req);

            final String ctx = req.getContextPath();
            boolean fromList = "list".equals(req.getParameter("from"));

            String redirectUrl = fromList
                    ? ctx + PROFILES_URL
                    : ctx + PROFILE_URL + "?id=" + id;

            String back = req.getParameter("back");
            if (SecurityRules.isSafeInternalRedirect(ctx, back, PROFILES_URL, PROFILE_URL)) {
                if (fromList || back.startsWith(ctx + PROFILES_URL)) {
                    redirectUrl = back;
                }
            }

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
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
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
                resp.sendRedirect(req.getContextPath() + LOGIN_URL);
                return;
            }
        } else {
            log.warn("[{}] Delete ignored (not found): id={}", rid(req), id);
        }

        resp.sendRedirect(req.getContextPath() + PROFILE_URL);
    }

    private static boolean isSafeBack(String back, String ctx) {
        if (back == null || back.isBlank()) return false;
        if (back.contains("\r") || back.contains("\n")) return false;
        String profiles = ctx + PROFILES_URL;
        String profile = ctx + PROFILE_URL;
        return back.startsWith(profiles) || back.startsWith(profile);
    }
}
