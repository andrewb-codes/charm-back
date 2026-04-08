package ru.andrewb.charm.back.controller.ui;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.PdfWriter;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import ru.andrewb.charm.back.dto.UserDetailsDto;
import ru.andrewb.charm.back.mapper.ProfileGetDtoToPdfMapper;
import ru.andrewb.charm.back.mapper.RequestToProfileUpdateDtoMapper;
import ru.andrewb.charm.back.model.exception.NotFoundException;
import ru.andrewb.charm.back.model.exception.OptimisticLockException;
import ru.andrewb.charm.back.model.exception.StorageException;
import ru.andrewb.charm.back.security.AuthUtils;
import ru.andrewb.charm.back.service.ProfileService;
import ru.andrewb.charm.back.validator.ProfileUpdateValidator;
import ru.andrewb.charm.back.web.flash.Flash;

import java.io.IOException;
import java.io.OutputStream;

import static ru.andrewb.charm.back.web.Urls.*;
import static ru.andrewb.charm.back.web.Views.getJspPath;

@Controller
public class ProfileController {

    private final ProfileService service;
    private final ProfileUpdateValidator validator;
    private final RequestToProfileUpdateDtoMapper requestToProfileUpdateDtoMapper;
    private final ProfileGetDtoToPdfMapper profileGetDtoToPdfMapper;

    public ProfileController(
            ProfileService service,
            ProfileUpdateValidator validator,
            RequestToProfileUpdateDtoMapper requestToProfileUpdateDtoMapper,
            ProfileGetDtoToPdfMapper profileGetDtoToPdfMapper
    ) {
        this.service = service;
        this.validator = validator;
        this.requestToProfileUpdateDtoMapper = requestToProfileUpdateDtoMapper;
        this.profileGetDtoToPdfMapper = profileGetDtoToPdfMapper;
    }

    @GetMapping(PROFILE_URL)
    public void getProfile(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            var authCtx = AuthUtils.getAuthCtx(req);
            if (authCtx == null) {
                resp.sendRedirect(req.getContextPath() + LOGIN_URL);
                return;
            }
            if (!authCtx.isAdmin() && authCtx.targetId() != authCtx.user().getId()) {
                resp.sendError(HttpServletResponse.SC_FORBIDDEN);
                return;
            }

            long id = authCtx.targetId();
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

            req.getRequestDispatcher(getJspPath(PROFILE_URL)).forward(req, resp);

        } catch (NotFoundException e) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, e.getMessage());
        }
    }

    @GetMapping(PROFILE_URL + "/pdf")
    public void downloadPdf(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            var authCtx = AuthUtils.getAuthCtx(req);
            if (authCtx == null) {
                resp.sendRedirect(req.getContextPath() + LOGIN_URL);
                return;
            }
            if (!authCtx.isAdmin() && authCtx.targetId() != authCtx.user().getId()) {
                resp.sendError(HttpServletResponse.SC_FORBIDDEN);
                return;
            }

            long id = authCtx.targetId();
            var dto = service.findByIdOrThrow(id);

            resp.setHeader("Content-Disposition", "attachment; filename=\"profile-" + id + ".pdf\"");
            resp.setContentType("application/pdf");

            Document pdf = new Document();
            try (OutputStream out = resp.getOutputStream()) {
                PdfWriter writer = PdfWriter.getInstance(pdf, out);

                pdf.open();
                profileGetDtoToPdfMapper.map(dto, pdf);
                pdf.close();

                writer.close();
                out.flush();
                resp.flushBuffer();
            } catch (DocumentException e) {
                resp.reset();
                resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "error.pdf.build");
            }

        } catch (NotFoundException e) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, e.getMessage());
        }
    }

    @PutMapping(PROFILE_URL)
    public void updateProfile(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            var authCtx = AuthUtils.getAuthCtx(req);
            if (authCtx == null) {
                resp.sendRedirect(req.getContextPath() + LOGIN_URL);
                return;
            }
            if (!authCtx.isAdmin() && authCtx.targetId() != authCtx.user().getId()) {
                resp.sendError(HttpServletResponse.SC_FORBIDDEN);
                return;
            }

            long id = authCtx.targetId();
            String redirect = authCtx.isAdmin()
                    ? req.getContextPath() + PROFILE_URL + "?id=" + id
                    : req.getContextPath() + PROFILE_URL;

            var dto = requestToProfileUpdateDtoMapper.map(req);
            var vr = validator.validate(dto);
            if (vr.isNotValid()) {
                vr.getErrors().forEach(code -> Flash.addError(req, code));
                Flash.putField(req, "name", dto.getName());
                Flash.putField(req, "surname", dto.getSurname());
                Flash.putField(req, "about", dto.getAbout());
                resp.sendRedirect(redirect);
                return;
            }

            service.update(id, dto);
            resp.sendRedirect(redirect);

        } catch (OptimisticLockException e) {
            Flash.addError(req, "error.optimistic-lock");
            resp.sendRedirect(req.getContextPath() + SETTINGS_URL);

        } catch (NotFoundException e) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, e.getMessage());

        } catch (StorageException e) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        }
    }

    @DeleteMapping(PROFILE_URL)
    public void deleteProfile(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        var authCtx = AuthUtils.getAuthCtx(req);
        if (authCtx == null) {
            resp.sendRedirect(req.getContextPath() + LOGIN_URL);
            return;
        }
        if (!authCtx.isAdmin() && authCtx.targetId() != authCtx.user().getId()) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        long id = authCtx.targetId();
        UserDetailsDto user = authCtx.user();

        boolean deleted = service.delete(id);
        if (deleted) {
            if (user.getId().equals(id)) {
                req.getSession(false).invalidate();
                resp.sendRedirect(req.getContextPath() + LOGIN_URL);
            } else {
                resp.sendRedirect(req.getContextPath() + PROFILES_URL);
            }
        } else {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }
}
