package ru.andrewb.charm.back.controller.ui;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.PdfWriter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
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
import ru.andrewb.charm.back.controller.request.ProfileUpdateRequest;
import ru.andrewb.charm.back.controller.ui.support.BindingErrors;
import ru.andrewb.charm.back.mapper.ProfileGetDtoToPdfMapper;
import ru.andrewb.charm.back.mapper.ProfileGetDtoToProfileUpdateRequestMapper;
import ru.andrewb.charm.back.mapper.ProfileUpdateRequestToCommandMapper;
import ru.andrewb.charm.back.model.exception.NotFoundException;
import ru.andrewb.charm.back.model.exception.OptimisticLockException;
import ru.andrewb.charm.back.security.AuthUser;
import ru.andrewb.charm.back.service.ProfileService;
import ru.andrewb.charm.back.service.command.ProfileUpdateCommand;

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
    private final ProfileGetDtoToPdfMapper profileGetDtoToPdfMapper;
    private final ProfileGetDtoToProfileUpdateRequestMapper profileGetDtoToRequestMapper;
    private final ProfileUpdateRequestToCommandMapper profileUpdateRequestToCommandMapper;

    public ProfileController(
            ProfileService service,
            ProfileGetDtoToPdfMapper profileGetDtoToPdfMapper,
            ProfileGetDtoToProfileUpdateRequestMapper profileGetDtoToRequestMapper,
            ProfileUpdateRequestToCommandMapper profileUpdateRequestToCommandMapper
    ) {
        this.service = service;
        this.profileGetDtoToPdfMapper = profileGetDtoToPdfMapper;
        this.profileGetDtoToRequestMapper = profileGetDtoToRequestMapper;
        this.profileUpdateRequestToCommandMapper = profileUpdateRequestToCommandMapper;
    }

    @GetMapping
    public String getProfile(
            @AuthenticationPrincipal AuthUser user,
            Model model
    ) {
        var dto = service.findByIdOrThrow(user.getId());
        model.addAttribute("profileGetDto", dto);

        if (!model.containsAttribute("profileUpdateRequest")) {
            ProfileUpdateRequest request = profileGetDtoToRequestMapper.map(dto);
            model.addAttribute("profileUpdateRequest", request);
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
            @Valid @ModelAttribute("profileUpdateRequest") ProfileUpdateRequest request,
            BindingResult br,
            RedirectAttributes ra
    ) {
        if (br.hasErrors()) {
            request.setPhoto(null);
            ra.addFlashAttribute("errors", BindingErrors.extract(br));
            ra.addFlashAttribute("profileUpdateRequest", request);
            return "redirect:" + PROFILE_URL;
        }

        ProfileUpdateCommand command = profileUpdateRequestToCommandMapper.map(request);

        try {
            service.update(user.getId(), command, request.getPhoto());
            return "redirect:" + PROFILE_URL;

        } catch (OptimisticLockException e) {
            ra.addFlashAttribute("errors", List.of("error.optimistic-lock"));
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
}
