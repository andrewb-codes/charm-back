package ru.andrewb.charm.back.controller.ui;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.andrewb.charm.back.dto.LoginDto;
import ru.andrewb.charm.back.dto.UserDetailsDto;
import ru.andrewb.charm.back.model.exception.BadRequestException;
import ru.andrewb.charm.back.security.AuthUtils;
import ru.andrewb.charm.back.service.ProfileService;
import ru.andrewb.charm.back.validator.LoginValidator;

import java.util.List;

import static ru.andrewb.charm.back.web.Urls.*;
import static ru.andrewb.charm.back.web.Views.LOGIN;

@Controller
public class LoginController {

    private final ProfileService service;
    private final LoginValidator validator;

    public LoginController(
            ProfileService service,
            LoginValidator validator
    ) {
        this.service = service;
        this.validator = validator;
    }

    @GetMapping(LOGIN_URL)
    public String getLoginPage(HttpServletRequest req, Model model) {
        UserDetailsDto user = AuthUtils.getUserOrNull(req);
        if (user != null) {
            return "redirect:" + PROFILE_URL;
        }

        if (!model.containsAttribute("loginDto")) {
            model.addAttribute("loginDto", new LoginDto());
        }

        return LOGIN;
    }

    @PostMapping(LOGIN_URL)
    public String login(
            @ModelAttribute("loginDto") LoginDto dto,
            HttpServletRequest req,
            RedirectAttributes redirectAttributes
    )  {
        var vr = validator.validate(dto);
        if (vr.isNotValid()) {
            redirectAttributes.addFlashAttribute("errors", vr.getErrors());
            redirectAttributes.addFlashAttribute("loginDto", dto);
            return "redirect:" + LOGIN_URL;
        }

        try {
            var userDetails = service.login(dto);

            req.changeSessionId();
            HttpSession session = req.getSession();
            session.setAttribute("userDetails", userDetails);

            return "redirect:" + INDEX_URL;

        } catch (BadRequestException e) {
            redirectAttributes.addFlashAttribute("errors", List.of(e.getMessage()));
            redirectAttributes.addFlashAttribute("loginDto", dto);
            return "redirect:" + LOGIN_URL;
        }
    }
}
