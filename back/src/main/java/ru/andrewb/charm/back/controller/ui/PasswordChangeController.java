package ru.andrewb.charm.back.controller.ui;

import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.andrewb.charm.back.controller.request.PasswordChangeRequest;
import ru.andrewb.charm.back.controller.ui.support.BindingErrors;
import ru.andrewb.charm.back.mapper.PasswordChangeRequestToCommandMapper;
import ru.andrewb.charm.back.model.exception.BadRequestException;
import ru.andrewb.charm.back.security.AuthUser;
import ru.andrewb.charm.back.service.ProfileService;
import ru.andrewb.charm.back.service.command.PasswordChangeCommand;

import java.util.List;

import static ru.andrewb.charm.back.web.Urls.PASSWORD_URL;
import static ru.andrewb.charm.back.web.Urls.SETTINGS_URL;

@Controller
@RequestMapping(PASSWORD_URL)
public class PasswordChangeController {

    private final ProfileService service;
    private final PasswordChangeRequestToCommandMapper mapper;

    public PasswordChangeController(
            ProfileService service,
            PasswordChangeRequestToCommandMapper mapper
    ) {
        this.service = service;
        this.mapper = mapper;
    }

    @PutMapping
    public String changePassword(
            @AuthenticationPrincipal AuthUser user,
            @Valid @ModelAttribute("passwordChangeRequest") PasswordChangeRequest request,
            BindingResult br,
            RedirectAttributes ra
    ) {
        if (br.hasErrors()) {
            request.setCurrentPassword(null);
            request.setNewPassword(null);
            request.setConfirmPassword(null);
            ra.addFlashAttribute("errors", BindingErrors.extract(br));
            ra.addFlashAttribute("passwordChangeRequest", request);
            return "redirect:" + SETTINGS_URL;
        }

        PasswordChangeCommand command = mapper.map(request);

        try {
            service.changePassword(user.getId(), command);
            return "redirect:" + SETTINGS_URL;

        } catch (BadRequestException e) {
            request.setCurrentPassword(null);
            request.setNewPassword(null);
            request.setConfirmPassword(null);
            ra.addFlashAttribute("errors", List.of(e.getMessage()));
            ra.addFlashAttribute("passwordChangeRequest", request);
            return "redirect:" + SETTINGS_URL;
        }
    }
}
