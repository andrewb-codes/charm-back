package ru.andrewb.charm.back.controller.ui;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import ru.andrewb.charm.back.dto.EmailChangeDto;
import ru.andrewb.charm.back.dto.PasswordChangeDto;
import ru.andrewb.charm.back.security.AuthUtils;
import ru.andrewb.charm.back.service.ProfileService;

import static ru.andrewb.charm.back.web.Urls.LOGIN_URL;
import static ru.andrewb.charm.back.web.Urls.SETTINGS_URL;
import static ru.andrewb.charm.back.web.Views.SETTINGS;

@Controller
public class SettingsController {

    private final ProfileService service;

    public SettingsController(ProfileService service) {
        this.service = service;
    }

    @GetMapping(SETTINGS_URL)
    public String getSettingsPage(
            HttpServletRequest req,
            Model model
    ) {
        var user = AuthUtils.getUserOrNull(req);
        if (user == null) {
            return "redirect:" + LOGIN_URL;
        }

        var profileDto = service.findByIdOrThrow(user.getId());
        model.addAttribute("profileDto", profileDto);

        if (!model.containsAttribute("emailChangeDto")) {
            EmailChangeDto emailChangeDto = new EmailChangeDto();
            emailChangeDto.setVersion(profileDto.getVersion());
            emailChangeDto.setNewEmail(profileDto.getEmail());
            model.addAttribute("emailChangeDto", emailChangeDto);
        }
        if (!model.containsAttribute("passwordChangeDto")) {
            PasswordChangeDto passwordChangeDto = new PasswordChangeDto();
            passwordChangeDto.setVersion(profileDto.getVersion());
            model.addAttribute("passwordChangeDto", passwordChangeDto);
        }

        return SETTINGS;
    }
}
