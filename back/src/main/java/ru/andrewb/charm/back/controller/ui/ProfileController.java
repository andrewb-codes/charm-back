package ru.andrewb.charm.back.controller.ui;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.PdfWriter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.andrewb.charm.back.controller.form.ProfileUpdateForm;
import ru.andrewb.charm.back.dto.ProfileGetDto;
import ru.andrewb.charm.back.dto.ProfileUpdateDto;
import ru.andrewb.charm.back.dto.UserDetailsDto;
import ru.andrewb.charm.back.mapper.ProfileGetDtoToPdfMapper;
import ru.andrewb.charm.back.model.exception.NotFoundException;
import ru.andrewb.charm.back.model.exception.OptimisticLockException;
import ru.andrewb.charm.back.security.AuthUtils;
import ru.andrewb.charm.back.service.ProfileService;
import ru.andrewb.charm.back.validator.ProfileUpdateValidator;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import static ru.andrewb.charm.back.web.Urls.*;
import static ru.andrewb.charm.back.web.Views.ERROR_403;
import static ru.andrewb.charm.back.web.Views.PROFILE;

@Controller
public class ProfileController {

    private final ProfileService service;
    private final ProfileUpdateValidator validator;
    private final ProfileGetDtoToPdfMapper profileGetDtoToPdfMapper;

    public ProfileController(
            ProfileService service,
            ProfileUpdateValidator validator,
            ProfileGetDtoToPdfMapper profileGetDtoToPdfMapper
    ) {
        this.service = service;
        this.validator = validator;
        this.profileGetDtoToPdfMapper = profileGetDtoToPdfMapper;
    }

    @GetMapping(PROFILE_URL)
    public String getProfile(
            HttpServletRequest req,
            Model model
    ) {
        var authCtx = AuthUtils.getAuthCtx(req);
        if (authCtx == null) {
            return "redirect:" + LOGIN_URL;
        }
        if (!authCtx.isAdmin() && authCtx.targetId() != authCtx.user().getId()) {
            return ERROR_403;
        }

        var profileGetDto = service.findByIdOrThrow(authCtx.targetId());
        model.addAttribute("profileGetDto", profileGetDto);

        if (!model.containsAttribute("profileUpdateForm")) {
            model.addAttribute("profileUpdateForm", toForm(profileGetDto));
        }

        return PROFILE;
    }

    @GetMapping(PROFILE_URL + "/pdf")
    public void downloadPdf(
            HttpServletRequest req,
            HttpServletResponse resp
    ) throws IOException {
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

    @PutMapping(value = PROFILE_URL, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public String updateProfile(
            @ModelAttribute("profileUpdateForm") ProfileUpdateForm form,
            BindingResult bindingResult,
            HttpServletRequest req,
            RedirectAttributes redirectAttributes
    ) {
        var authCtx = AuthUtils.getAuthCtx(req);
        if (authCtx == null) {
            return "redirect:" + LOGIN_URL;
        }
        if (!authCtx.isAdmin() && authCtx.targetId() != authCtx.user().getId()) {
            return ERROR_403;
        }

        long id = authCtx.targetId();
        String redirect = authCtx.isAdmin() ? PROFILE_URL + "?id=" + id : PROFILE_URL;

        if (bindingResult.hasErrors()) {
            form.setPhoto(null);
            redirectAttributes.addFlashAttribute("errors", List.of("error.param.invalid"));
            redirectAttributes.addFlashAttribute("profileUpdateForm", form);
            return "redirect:" + redirect;
        }

        ProfileUpdateDto dto = toDto(form);
        var vr = validator.validate(dto);
        if (vr.isNotValid()) {
            form.setPhoto(null);
            redirectAttributes.addFlashAttribute("errors", vr.getErrors());
            redirectAttributes.addFlashAttribute("profileUpdateForm", form);
            return "redirect:" + redirect;
        }

        try {
            service.update(id, dto, form.getPhoto());
            return "redirect:" + redirect;

        } catch (OptimisticLockException e) {
            redirectAttributes.addFlashAttribute("errors", List.of("error.optimistic-lock"));
            return "redirect:" + SETTINGS_URL;
        }
    }

    @DeleteMapping(PROFILE_URL)
    public String deleteProfile(HttpServletRequest req) {
        var authCtx = AuthUtils.getAuthCtx(req);
        if (authCtx == null) {
            return "redirect:" + LOGIN_URL;
        }
        if (!authCtx.isAdmin() && authCtx.targetId() != authCtx.user().getId()) {
            return ERROR_403;
        }

        long id = authCtx.targetId();
        UserDetailsDto user = authCtx.user();

        service.delete(id);

        if (user.getId().equals(id)) {
            req.getSession(false).invalidate();
            return "redirect:" + LOGIN_URL;
        }
        return "redirect:" + PROFILES_URL;
    }

    private ProfileUpdateForm toForm(ProfileGetDto profile) {
        ProfileUpdateForm form = new ProfileUpdateForm();
        form.setName(profile.getName());
        form.setSurname(profile.getSurname());
        form.setAbout(profile.getAbout());
        form.setBirthdate(profile.getBirthdate());
        form.setGender(profile.getGender());
        form.setVersion(profile.getVersion());
        return form;
    }

    private ProfileUpdateDto toDto(ProfileUpdateForm form) {
        ProfileUpdateDto dto = new ProfileUpdateDto();
        dto.setName(form.getName());
        dto.setSurname(form.getSurname());
        dto.setAbout(form.getAbout());
        dto.setBirthdate(form.getBirthdate());
        dto.setGender(form.getGender());
        dto.setVersion(form.getVersion());
        return dto;
    }
}
