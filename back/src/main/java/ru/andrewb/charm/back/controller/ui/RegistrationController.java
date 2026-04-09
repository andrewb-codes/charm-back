package ru.andrewb.charm.back.controller.ui;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.andrewb.charm.back.dto.RegistrationDto;
import ru.andrewb.charm.back.model.exception.BadRequestException;
import ru.andrewb.charm.back.model.exception.DuplicateEmailException;
import ru.andrewb.charm.back.service.ProfileService;
import ru.andrewb.charm.back.validator.RegistrationValidator;

import java.util.List;

import static ru.andrewb.charm.back.web.Urls.LOGIN_URL;
import static ru.andrewb.charm.back.web.Urls.REGISTRATION_URL;
import static ru.andrewb.charm.back.web.Views.REGISTRATION;

@Controller
public class RegistrationController {

    private final ProfileService service;
    private final RegistrationValidator validator;

    public RegistrationController(
            ProfileService service,
            RegistrationValidator validator
    ) {
        this.service = service;
        this.validator = validator;
    }

    @GetMapping(REGISTRATION_URL)
    public String getRegistrationPage(Model model) {
        if (!model.containsAttribute("registrationDto")) {
            model.addAttribute("registrationDto", new RegistrationDto());
        }
        return REGISTRATION;
    }

    @PostMapping(REGISTRATION_URL)
    public String register(
            @ModelAttribute("registrationDto") RegistrationDto dto,
            RedirectAttributes redirectAttributes
    ) {
        var vr = validator.validate(dto);
        if (vr.isNotValid()) {
            dto.setPassword(null);
            redirectAttributes.addFlashAttribute("errors", vr.getErrors());
            redirectAttributes.addFlashAttribute("registrationDto", dto);
            return "redirect:" + REGISTRATION_URL;
        }

        try {
            service.save(dto);
            return "redirect:" + LOGIN_URL;

        } catch (DuplicateEmailException | BadRequestException e) {
            dto.setPassword(null);
            redirectAttributes.addFlashAttribute("errors", List.of(e.getMessage()));
            redirectAttributes.addFlashAttribute("registrationDto", dto);
            return "redirect:" + REGISTRATION_URL;
        }
    }
}
