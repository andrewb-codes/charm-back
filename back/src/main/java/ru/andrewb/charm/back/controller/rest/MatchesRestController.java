package ru.andrewb.charm.back.controller.rest;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.andrewb.charm.back.dto.ProfileGetDto;
import ru.andrewb.charm.back.security.AuthUser;
import ru.andrewb.charm.back.service.ProfileService;

import java.util.List;

import static ru.andrewb.charm.back.web.Urls.MATCHES_REST_URL;

@RestController
@RequestMapping(MATCHES_REST_URL)
public class MatchesRestController {

    private final ProfileService service;

    public MatchesRestController(ProfileService service) {
        this.service = service;
    }

    public record MatchesResponse(List<ProfileGetDto> items, boolean hasNext) {}

    @GetMapping
    public ResponseEntity<?> getMatches(
            @AuthenticationPrincipal AuthUser user,
            @RequestParam(name = "page", required = false) Integer page,
            @RequestParam(name = "pageSize", required = false) Integer pageSize
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

        return ResponseEntity.ok(new MatchesResponse(items, hasNext));
    }
}
