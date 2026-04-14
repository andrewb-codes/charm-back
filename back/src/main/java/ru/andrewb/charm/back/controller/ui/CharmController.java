package ru.andrewb.charm.back.controller.ui;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.andrewb.charm.back.dto.Action;
import ru.andrewb.charm.back.dto.CharmDto;
import ru.andrewb.charm.back.normalizer.CharmDtoDefaults;
import ru.andrewb.charm.back.security.AuthUser;
import ru.andrewb.charm.back.service.CharmService;

import static ru.andrewb.charm.back.web.Urls.CHARM_URL;
import static ru.andrewb.charm.back.web.Views.CHARM;
import static ru.andrewb.charm.back.web.Views.CHARM_EMPTY;

@Controller
@RequestMapping(CHARM_URL)
public class CharmController {

    private final CharmService service;

    public CharmController(CharmService service) {
        this.service = service;
    }

    @GetMapping
    public String getCharm(
            @AuthenticationPrincipal AuthUser user,
            Model model
    ) {
        return handle(user,null, null, model);
    }

    @PostMapping
    public String postCharm(
            @AuthenticationPrincipal AuthUser user,
            @RequestParam(name = "action", required = false) Action action,
            @RequestParam(name = "toProfileId", required = false) Long toProfileId,
            Model model
    ) {
        return handle(user, action, toProfileId, model);
    }

    private String handle(
            AuthUser user,
            Action action,
            Long toProfileId,
            Model model
    ) {
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

