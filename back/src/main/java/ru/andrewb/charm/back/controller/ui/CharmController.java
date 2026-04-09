package ru.andrewb.charm.back.controller.ui;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.andrewb.charm.back.dto.Action;
import ru.andrewb.charm.back.dto.CharmDto;
import ru.andrewb.charm.back.normalizer.CharmDtoDefaults;
import ru.andrewb.charm.back.security.AuthUtils;
import ru.andrewb.charm.back.service.CharmService;

import static ru.andrewb.charm.back.web.Urls.CHARM_URL;
import static ru.andrewb.charm.back.web.Urls.LOGIN_URL;
import static ru.andrewb.charm.back.web.Views.CHARM;
import static ru.andrewb.charm.back.web.Views.CHARM_EMPTY;

@Controller
public class CharmController {

    private final CharmService service;

    public CharmController(CharmService service) {
        this.service = service;
    }

    @GetMapping(CHARM_URL)
    public String getCharm(HttpServletRequest req, Model model) {
        return handle(null, null, req, model);
    }

    @PostMapping(CHARM_URL)
    public String postCharm(
            @RequestParam(name = "action", required = false) Action action,
            @RequestParam(name = "toProfileId", required = false) Long toProfileId,
            HttpServletRequest req,
            Model model
    ) {
        return handle(action, toProfileId, req, model);
    }

    private String handle(
            Action action,
            Long toProfileId,
            HttpServletRequest req,
            Model model
    ) {
        var user = AuthUtils.getUserOrNull(req);
        if (user == null) {
            return "redirect:" + LOGIN_URL;
        }

        var charmDto = new CharmDto();
        charmDto.setFromProfileId(user.getId());
        charmDto.setAction(action);
        charmDto.setToProfileId(toProfileId);
        CharmDtoDefaults.normalize(charmDto);
        
        var nextOpt = service.getNext(charmDto);
        if (nextOpt.isPresent()) {
            model.addAttribute("next", nextOpt.get());
            return CHARM;
        }

        return CHARM_EMPTY;
    }
}

