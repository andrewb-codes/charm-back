package ru.andrewb.charm.back.controller.ui;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.PdfWriter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.andrewb.charm.back.controller.form.ProfileUpdateForm;
import ru.andrewb.charm.back.dto.ProfileGetDto;
import ru.andrewb.charm.back.dto.ProfileUpdateDto;
import ru.andrewb.charm.back.mapper.ProfileGetDtoToPdfMapper;
import ru.andrewb.charm.back.model.exception.NotFoundException;
import ru.andrewb.charm.back.model.exception.OptimisticLockException;
import ru.andrewb.charm.back.security.AuthUser;
import ru.andrewb.charm.back.service.ProfileService;
import ru.andrewb.charm.back.validator.ProfileUpdateValidator;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import static ru.andrewb.charm.back.web.Urls.LOGIN_URL;
import static ru.andrewb.charm.back.web.Urls.PROFILE_URL;
import static ru.andrewb.charm.back.web.Views.PROFILE;

@Controller
@RequestMapping(PROFILE_URL)
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

    @GetMapping
    public String getProfile(
            @AuthenticationPrincipal AuthUser user,
            Model model
    ) {
        var profileGetDto = service.findByIdOrThrow(user.getId());
        model.addAttribute("profileGetDto", profileGetDto);

        if (!model.containsAttribute("profileUpdateForm")) {
            model.addAttribute("profileUpdateForm", toForm(profileGetDto));
        }

        model.addAttribute("profileAction", PROFILE_URL);
        model.addAttribute("profilePdfUrl", PROFILE_URL + "/pdf");
        model.addAttribute("showSettingsLink", true);
        model.addAttribute("showDeleteButton", false);

        return PROFILE;
    }

    @GetMapping("/pdf")
    public void downloadPdf(
            @AuthenticationPrincipal AuthUser user,
            HttpServletResponse resp
    ) throws IOException {
        try {
            var dto = service.findByIdOrThrow(user.getId());

            resp.setHeader("Content-Disposition", "attachment; filename=\"profile-" + user.getId() + ".pdf\"");
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

    @PutMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public String updateProfile(
            @AuthenticationPrincipal AuthUser user,
            @ModelAttribute("profileUpdateForm") ProfileUpdateForm form,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes
    ) {

        if (bindingResult.hasErrors()) {
            form.setPhoto(null);
            redirectAttributes.addFlashAttribute("errors", List.of("error.param.invalid"));
            redirectAttributes.addFlashAttribute("profileUpdateForm", form);
            return "redirect:" + PROFILE_URL;
        }

        ProfileUpdateDto dto = toDto(form);
        var vr = validator.validate(dto);
        if (vr.isNotValid()) {
            form.setPhoto(null);
            redirectAttributes.addFlashAttribute("errors", vr.getErrors());
            redirectAttributes.addFlashAttribute("profileUpdateForm", form);
            return "redirect:" + PROFILE_URL;
        }

        try {
            service.update(user.getId(), dto, form.getPhoto());
            return "redirect:" + PROFILE_URL;

        } catch (OptimisticLockException e) {
            redirectAttributes.addFlashAttribute("errors", List.of("error.optimistic-lock"));
            return "redirect:" + PROFILE_URL;
        }
    }

    @DeleteMapping
    public String deleteProfile(
            @AuthenticationPrincipal AuthUser user,
            HttpServletRequest req,
            HttpServletResponse resp
    ) {
        service.delete(user.getId());
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        new SecurityContextLogoutHandler().logout(req, resp, auth);
        return "redirect:" + LOGIN_URL;
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
