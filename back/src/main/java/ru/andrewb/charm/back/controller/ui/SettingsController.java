package ru.andrewb.charm.back.controller.ui;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import ru.andrewb.charm.back.controller.request.EmailChangeRequest;
import ru.andrewb.charm.back.controller.request.PasswordChangeRequest;
import ru.andrewb.charm.back.security.AuthUser;
import ru.andrewb.charm.back.service.ProfileService;

import static ru.andrewb.charm.back.web.Urls.SETTINGS_URL;
import static ru.andrewb.charm.back.web.Views.SETTINGS;

@Controller
@RequestMapping(SETTINGS_URL)
public class SettingsController {

    private final ProfileService service;

    public SettingsController(ProfileService service) {
        this.service = service;
    }

    @GetMapping
    public String getSettingsPage(
            @AuthenticationPrincipal AuthUser user,
            Model model
    ) {
        var dto = service.findByIdOrThrow(user.getId());
        model.addAttribute("profileGetDto", dto);

        if (!model.containsAttribute("emailChangeRequest")) {
            EmailChangeRequest emailChangeRequest = new EmailChangeRequest();
            emailChangeRequest.setVersion(dto.getVersion());
            emailChangeRequest.setNewEmail(dto.getEmail());
            model.addAttribute("emailChangeRequest", emailChangeRequest);
        }
        if (!model.containsAttribute("passwordChangeRequest")) {
            PasswordChangeRequest passwordChangeRequest = new PasswordChangeRequest();
            passwordChangeRequest.setVersion(dto.getVersion());
            model.addAttribute("passwordChangeRequest", passwordChangeRequest);
        }

        return SETTINGS;
    }
}
