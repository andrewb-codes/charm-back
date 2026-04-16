package ru.andrewb.charm.back.controller.ui;

import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import ru.andrewb.charm.back.controller.request.CharmRequest;
import ru.andrewb.charm.back.mapper.CharmRequestToCommandMapper;
import ru.andrewb.charm.back.normalizer.CharmCommandDefaults;
import ru.andrewb.charm.back.security.AuthUser;
import ru.andrewb.charm.back.service.CharmService;
import ru.andrewb.charm.back.service.command.CharmCommand;

import static ru.andrewb.charm.back.web.Urls.CHARM_URL;
import static ru.andrewb.charm.back.web.Views.CHARM;
import static ru.andrewb.charm.back.web.Views.CHARM_EMPTY;

@Controller
@RequestMapping(CHARM_URL)
public class CharmController {

    private final CharmService service;
    private final CharmRequestToCommandMapper mapper;

    public CharmController(
            CharmService service,
            CharmRequestToCommandMapper mapper
    ) {
        this.service = service;
        this.mapper = mapper;
    }

    @GetMapping
    public String getCharm(
            @AuthenticationPrincipal AuthUser user,
            Model model
    ) {
        return handle(user,new CharmRequest(), model);
    }

    @PostMapping
    public String postCharm(
            @AuthenticationPrincipal AuthUser user,
            @Valid @ModelAttribute("charmRequest") CharmRequest request,
            BindingResult br,
            Model model
    ) {
        if (br.hasErrors()) {
            return CHARM_EMPTY;
        }
        return handle(user, request, model);
    }

    private String handle(
            AuthUser user,
            CharmRequest request,
            Model model
    ) {
        CharmCommand command = mapper.map(request);
        command.setFromProfileId(user.getId());
        CharmCommandDefaults.normalize(command);
        
        var nextOpt = service.getNext(command);
        if (nextOpt.isPresent()) {
            model.addAttribute("next", nextOpt.get());
            return CHARM;
        }

        return CHARM_EMPTY;
    }
}

