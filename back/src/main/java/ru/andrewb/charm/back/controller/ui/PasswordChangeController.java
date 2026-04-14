package ru.andrewb.charm.back.controller.ui;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.andrewb.charm.back.dto.PasswordChangeDto;
import ru.andrewb.charm.back.model.exception.BadRequestException;
import ru.andrewb.charm.back.security.AuthUser;
import ru.andrewb.charm.back.service.ProfileService;
import ru.andrewb.charm.back.validator.PasswordChangeValidator;

import java.util.List;

import static ru.andrewb.charm.back.web.Urls.PASSWORD_URL;
import static ru.andrewb.charm.back.web.Urls.SETTINGS_URL;

@Controller
@RequestMapping(PASSWORD_URL)
public class PasswordChangeController {

    private final ProfileService service;
    private final PasswordChangeValidator validator;

    public PasswordChangeController(
            ProfileService service,
            PasswordChangeValidator validator
    ) {
        this.service = service;
        this.validator = validator;
    }

    @PutMapping
    public String changePassword(
            @AuthenticationPrincipal AuthUser user,
            @ModelAttribute("passwordChangeDto") PasswordChangeDto dto,
            RedirectAttributes redirectAttributes
    ) {
        var vr = validator.validate(dto);
        if (vr.isNotValid()) {
            dto.setCurrentPassword(null);
            dto.setNewPassword(null);
            dto.setConfirmPassword(null);
            redirectAttributes.addFlashAttribute("errors", vr.getErrors());
            redirectAttributes.addFlashAttribute("passwordChangeDto", dto);
            return "redirect:" + SETTINGS_URL;
        }

        try {
            service.changePassword(user.getId(), dto);
            return "redirect:" + SETTINGS_URL;

        } catch (BadRequestException e) {
            dto.setCurrentPassword(null);
            dto.setNewPassword(null);
            dto.setConfirmPassword(null);
            redirectAttributes.addFlashAttribute("errors", List.of(e.getMessage()));
            redirectAttributes.addFlashAttribute("passwordChangeDto", dto);
            return "redirect:" + SETTINGS_URL;
        }
    }
}
