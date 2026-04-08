package ru.andrewb.charm.back.controller.ui;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.andrewb.charm.back.dto.ProfileFilter;
import ru.andrewb.charm.back.security.AuthUtils;
import ru.andrewb.charm.back.service.ProfileService;

import static ru.andrewb.charm.back.web.Urls.LOGIN_URL;
import static ru.andrewb.charm.back.web.Urls.MATCHES_URL;
import static ru.andrewb.charm.back.web.Views.MATCHES;

@Controller
public class MatchesController {

    private final ProfileService service;

    public MatchesController(ProfileService service) {
        this.service = service;
    }

    @GetMapping(MATCHES_URL)
    public String getMatches(
            @RequestParam(name = "page", required = false) Integer page,
            @RequestParam(name = "pageSize", required = false) Integer pageSize,
            HttpServletRequest req
    ) {
        var user = AuthUtils.getUserOrNull(req);
        if (user == null) {
            return "redirect:" + LOGIN_URL;
        }

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

        req.setAttribute("matches", items);
        req.setAttribute("filter", filter);
        req.setAttribute("hasPrev", hasPrev);
        req.setAttribute("hasNext", hasNext);

        return MATCHES;
    }
}
