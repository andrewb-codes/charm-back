package ru.andrewb.charm.back.controller.ui;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.andrewb.charm.back.dto.EmailChangeDto;
import ru.andrewb.charm.back.model.exception.BadRequestException;
import ru.andrewb.charm.back.model.exception.DuplicateEmailException;
import ru.andrewb.charm.back.model.exception.NotFoundException;
import ru.andrewb.charm.back.security.AuthUtils;
import ru.andrewb.charm.back.service.ProfileService;
import ru.andrewb.charm.back.validator.EmailChangeValidator;

import java.util.List;

import static ru.andrewb.charm.back.web.Urls.*;

@Controller
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

    @PutMapping(EMAIL_URL)
    public String changeEmail(
            @ModelAttribute("emailChangeDto") EmailChangeDto dto,
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

        } catch (NotFoundException e) {
            throw e;
        }
    }
}
