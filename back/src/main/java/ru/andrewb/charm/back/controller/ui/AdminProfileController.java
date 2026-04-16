package ru.andrewb.charm.back.controller.ui;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.PdfWriter;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
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
import ru.andrewb.charm.back.service.ProfileService;
import ru.andrewb.charm.back.service.command.ProfileUpdateCommand;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import static ru.andrewb.charm.back.web.Urls.ADMIN_PROFILES_URL;
import static ru.andrewb.charm.back.web.Views.PROFILE;

@Controller
@RequestMapping(ADMIN_PROFILES_URL)
@PreAuthorize("hasRole('ADMIN')")
public class AdminProfileController {

    private final ProfileService service;
    private final ProfileGetDtoToPdfMapper profileGetDtoToPdfMapper;
    private final ProfileGetDtoToProfileUpdateRequestMapper profileGetDtoToRequestMapper;
    private final ProfileUpdateRequestToCommandMapper profileUpdateRequestToCommandMapper;

    public AdminProfileController(
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

    @GetMapping("/{id}")
    public String getProfile(
            @PathVariable("id") Long id,
            Model model
    ) {
        var dto = service.findByIdOrThrow(id);
        model.addAttribute("profileGetDto", dto);

        if (!model.containsAttribute("profileUpdateRequest")) {
            ProfileUpdateRequest request = profileGetDtoToRequestMapper.map(dto);
            model.addAttribute("profileUpdateRequest", request);
        }

        model.addAttribute("profileAction", ADMIN_PROFILES_URL + "/" + id);
        model.addAttribute("profilePdfUrl", ADMIN_PROFILES_URL + "/" + id + "/pdf");
        model.addAttribute("showSettingsLink", false);
        model.addAttribute("showDeleteButton", true);
        model.addAttribute("deleteAction", ADMIN_PROFILES_URL + "/" + id);

        return PROFILE;
    }

    @GetMapping("/{id}/pdf")
    public void downloadPdf(
            @PathVariable("id") Long id,
            HttpServletResponse resp
    ) throws IOException {
        try {
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

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public String updateProfile(
            @PathVariable("id") Long id,
            @Valid @ModelAttribute("profileUpdateRequest") ProfileUpdateRequest request,
            BindingResult br,
            RedirectAttributes ra
    ) {
        if (br.hasErrors()) {
            request.setPhoto(null);
            ra.addFlashAttribute("errors", BindingErrors.extract(br));
            ra.addFlashAttribute("profileUpdateRequest", request);
            return "redirect:" + ADMIN_PROFILES_URL + "/" + id;
        }

        ProfileUpdateCommand command = profileUpdateRequestToCommandMapper.map(request);

        try {
            service.update(id, command, request.getPhoto());
            return "redirect:" + ADMIN_PROFILES_URL + "/" + id;

        } catch (OptimisticLockException e) {
            ra.addFlashAttribute("errors", List.of("error.optimistic-lock"));
            return "redirect:" + ADMIN_PROFILES_URL + "/" + id;
        }
    }

    @DeleteMapping("/{id}")
    public String deleteProfile(@PathVariable("id") Long id) {
        service.delete(id);
        return "redirect:" + ADMIN_PROFILES_URL;
    }
}
