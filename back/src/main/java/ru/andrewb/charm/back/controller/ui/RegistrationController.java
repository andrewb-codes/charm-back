package ru.andrewb.charm.back.controller.ui;

import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.andrewb.charm.back.controller.request.RegistrationRequest;
import ru.andrewb.charm.back.controller.ui.support.BindingErrors;
import ru.andrewb.charm.back.mapper.RegistrationRequestToCommandMapper;
import ru.andrewb.charm.back.model.exception.BadRequestException;
import ru.andrewb.charm.back.model.exception.DuplicateEmailException;
import ru.andrewb.charm.back.service.ProfileService;
import ru.andrewb.charm.back.service.command.RegistrationCommand;

import java.util.List;

import static ru.andrewb.charm.back.web.Urls.LOGIN_URL;
import static ru.andrewb.charm.back.web.Urls.REGISTRATION_URL;
import static ru.andrewb.charm.back.web.Views.REGISTRATION;

@Controller
@RequestMapping(REGISTRATION_URL)
public class RegistrationController {

    private final ProfileService service;
    private final RegistrationRequestToCommandMapper mapper;

    public RegistrationController(
            ProfileService service,
            RegistrationRequestToCommandMapper mapper
    ) {
        this.service = service;
        this.mapper = mapper;
    }

    @GetMapping
    public String getRegistrationPage(Model model) {
        if (!model.containsAttribute("registrationRequest")) {
            model.addAttribute("registrationRequest", new RegistrationRequest());
        }
        return REGISTRATION;
    }

    @PostMapping
    public String register(
            @Valid @ModelAttribute("registrationRequest") RegistrationRequest request,
            BindingResult br,
            RedirectAttributes ra
    ) {
        if (br.hasErrors()) {
            request.setPassword(null);
            ra.addFlashAttribute("errors", BindingErrors.extract(br));
            ra.addFlashAttribute("registrationRequest", request);
            return "redirect:" + REGISTRATION_URL;
        }

        RegistrationCommand command = mapper.map(request);

        try {
            service.save(command);
            return "redirect:" + LOGIN_URL;

        } catch (DuplicateEmailException | BadRequestException e) {
            request.setPassword(null);
            ra.addFlashAttribute("errors", List.of(e.getMessage()));
            ra.addFlashAttribute("registrationRequest", request);
            return "redirect:" + REGISTRATION_URL;
        }
    }
}
