package ru.andrewb.charm.back.controller.ui;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

import static ru.andrewb.charm.back.web.Urls.LOGIN_URL;
import static ru.andrewb.charm.back.web.Views.LOGIN;

@Controller
@RequestMapping(LOGIN_URL)
public class LoginController {

    @GetMapping
    public String getLoginPage(
            @RequestParam(name = "error", required = false) String error,
            @RequestParam(name = "logout", required = false) String logout,
            Model model
    ) {
        if (error != null) {
            model.addAttribute("errors", List.of("error.login.bad-credentials"));
        }
        if (logout != null) {
            model.addAttribute("message", List.of("message.logout-success"));
        }
        return LOGIN;
    }
}
