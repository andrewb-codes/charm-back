package ru.andrewb.charm.back.controller.ui;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.andrewb.charm.back.dto.PasswordChangeDto;
import ru.andrewb.charm.back.model.exception.BadRequestException;
import ru.andrewb.charm.back.security.AuthUtils;
import ru.andrewb.charm.back.service.ProfileService;
import ru.andrewb.charm.back.validator.PasswordChangeValidator;

import java.util.List;

import static ru.andrewb.charm.back.web.Urls.*;

@Controller
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

    @PutMapping(PASSWORD_URL)
    public String changePassword(
            @ModelAttribute("passwordChangeDto") PasswordChangeDto dto,
            HttpServletRequest req,
            RedirectAttributes redirectAttributes
    ) {
        var user = AuthUtils.getUserOrNull(req);
        if (user == null) {
            return "redirect:" + LOGIN_URL;
        }

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
