package ru.andrewb.charm.back.controller.ui;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.andrewb.charm.back.dto.ProfileFilter;
import ru.andrewb.charm.back.security.AuthUser;
import ru.andrewb.charm.back.service.ProfileService;

import static ru.andrewb.charm.back.web.Urls.MATCHES_URL;
import static ru.andrewb.charm.back.web.Views.MATCHES;

@Controller
@RequestMapping(MATCHES_URL)
public class MatchesController {

    private final ProfileService service;

    public MatchesController(ProfileService service) {
        this.service = service;
    }

    @GetMapping
    public String getMatches(
            @AuthenticationPrincipal AuthUser user,
            @RequestParam(name = "page", required = false) Integer page,
            @RequestParam(name = "pageSize", required = false) Integer pageSize,
            Model model
    ) {
        int normalizedPage = (page == null || page < 1) ? 1 : page;
        int normalizedPageSize = (pageSize == null || pageSize < 1) ? 10 : pageSize;

        int limit = normalizedPageSize + 1;
        int offset = (normalizedPage - 1) * normalizedPageSize;

        var items = service.findMatches(user.getId(), limit, offset);
        boolean hasNext = items.size() > normalizedPageSize;
        if (hasNext) {
            items = items.subList(0, normalizedPageSize);
        }

        boolean hasPrev = normalizedPage > 1;

        ProfileFilter filter = new ProfileFilter();
        filter.setPage(normalizedPage);
        filter.setPageSize(normalizedPageSize);

        model.addAttribute("matches", items);
        model.addAttribute("filter", filter);
        model.addAttribute("hasPrev", hasPrev);
        model.addAttribute("hasNext", hasNext);

        return MATCHES;
    }
}
