package ru.andrewb.charm.back.controller.ui;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import ru.andrewb.charm.back.dto.EmailChangeDto;
import ru.andrewb.charm.back.dto.PasswordChangeDto;
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
        var profileGetDto = service.findByIdOrThrow(user.getId());
        model.addAttribute("profileGetDto", profileGetDto);

        if (!model.containsAttribute("emailChangeDto")) {
            EmailChangeDto emailChangeDto = new EmailChangeDto();
            emailChangeDto.setVersion(profileGetDto.getVersion());
            emailChangeDto.setNewEmail(profileGetDto.getEmail());
            model.addAttribute("emailChangeDto", emailChangeDto);
        }
        if (!model.containsAttribute("passwordChangeDto")) {
            PasswordChangeDto passwordChangeDto = new PasswordChangeDto();
            passwordChangeDto.setVersion(profileGetDto.getVersion());
            model.addAttribute("passwordChangeDto", passwordChangeDto);
        }

        return SETTINGS;
    }
}
