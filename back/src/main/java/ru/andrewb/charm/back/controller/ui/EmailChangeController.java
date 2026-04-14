package ru.andrewb.charm.back.controller.ui;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.andrewb.charm.back.dto.EmailChangeDto;
import ru.andrewb.charm.back.model.exception.BadRequestException;
import ru.andrewb.charm.back.model.exception.DuplicateEmailException;
import ru.andrewb.charm.back.security.AuthUser;
import ru.andrewb.charm.back.service.ProfileService;
import ru.andrewb.charm.back.validator.EmailChangeValidator;

import java.util.List;

import static ru.andrewb.charm.back.web.Urls.EMAIL_URL;
import static ru.andrewb.charm.back.web.Urls.SETTINGS_URL;

@Controller
@RequestMapping(EMAIL_URL)
public class EmailChangeController {

    private final ProfileService service;
    private final EmailChangeValidator validator;

    public EmailChangeController(
            ProfileService service,
            EmailChangeValidator validator
    ) {
        this.service = service;
        this.validator = validator;
    }

    @PutMapping
    public String changeEmail(
            @AuthenticationPrincipal AuthUser user,
            @ModelAttribute("emailChangeDto") EmailChangeDto dto,
            RedirectAttributes redirectAttributes
    ) {
        var vr = validator.validate(dto);
        if (vr.isNotValid()) {
            dto.setCurrentPassword(null);
            redirectAttributes.addFlashAttribute("errors", vr.getErrors());
            redirectAttributes.addFlashAttribute("emailChangeDto", dto);
            return "redirect:" + SETTINGS_URL;
        }

        try {
            service.changeEmail(user.getId(), dto);
            return "redirect:" + SETTINGS_URL;

        }  catch (BadRequestException | DuplicateEmailException e) {
            dto.setCurrentPassword(null);
            redirectAttributes.addFlashAttribute("errors", List.of(e.getMessage()));
            redirectAttributes.addFlashAttribute("emailChangeDto", dto);
            return "redirect:" + SETTINGS_URL;
        }
    }
}
