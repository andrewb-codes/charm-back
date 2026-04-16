package ru.andrewb.charm.back.controller.ui;

import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.andrewb.charm.back.controller.request.EmailChangeRequest;
import ru.andrewb.charm.back.controller.ui.support.BindingErrors;
import ru.andrewb.charm.back.mapper.EmailChangeRequestToCommandMapper;
import ru.andrewb.charm.back.model.exception.BadRequestException;
import ru.andrewb.charm.back.model.exception.DuplicateEmailException;
import ru.andrewb.charm.back.security.AuthUser;
import ru.andrewb.charm.back.service.ProfileService;
import ru.andrewb.charm.back.service.command.EmailChangeCommand;

import java.util.List;

import static ru.andrewb.charm.back.web.Urls.EMAIL_URL;
import static ru.andrewb.charm.back.web.Urls.SETTINGS_URL;

@Controller
@RequestMapping(EMAIL_URL)
public class EmailChangeController {

    private final ProfileService service;
    private final EmailChangeRequestToCommandMapper mapper;

    public EmailChangeController(
            ProfileService service,
            EmailChangeRequestToCommandMapper mapper
    ) {
        this.service = service;
        this.mapper = mapper;
    }

    @PutMapping
    public String changeEmail(
            @AuthenticationPrincipal AuthUser user,
            @Valid @ModelAttribute("emailChangeRequest") EmailChangeRequest request,
            BindingResult br,
            RedirectAttributes ra
    ) {
        if (br.hasErrors()) {
            request.setCurrentPassword(null);
            ra.addFlashAttribute("errors", BindingErrors.extract(br));
            ra.addFlashAttribute("emailChangeRequest", request);
            return "redirect:" + SETTINGS_URL;
        }

        EmailChangeCommand command = mapper.map(request);

        try {
            service.changeEmail(user.getId(), command);
            return "redirect:" + SETTINGS_URL;

        }  catch (BadRequestException | DuplicateEmailException e) {
            request.setCurrentPassword(null);
            ra.addFlashAttribute("errors", List.of(e.getMessage()));
            ra.addFlashAttribute("emailChangeRequest", request);
            return "redirect:" + SETTINGS_URL;
        }
    }
}
