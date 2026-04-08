package ru.andrewb.charm.back.controller.rest;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.andrewb.charm.back.dto.ProfileGetDto;
import ru.andrewb.charm.back.security.AuthUtils;
import ru.andrewb.charm.back.service.ProfileService;

import java.util.List;

import static ru.andrewb.charm.back.web.Urls.MATCHES_REST_URL;

@RestController
public class MatchesRestController {

    private final ProfileService service;

    public MatchesRestController(ProfileService service) {
        this.service = service;
    }

    public record MatchesResponse(List<ProfileGetDto> items, boolean hasNext) {}

    @GetMapping(MATCHES_REST_URL)
    public ResponseEntity<?> getMatches(
            @RequestParam(name = "page", required = false) Integer page,
            @RequestParam(name = "pageSize", required = false) Integer pageSize,
            HttpServletRequest req
    ) {
        var user = AuthUtils.getUserOrNull(req);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
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

        return ResponseEntity.ok(new MatchesResponse(items, hasNext));
    }
}
