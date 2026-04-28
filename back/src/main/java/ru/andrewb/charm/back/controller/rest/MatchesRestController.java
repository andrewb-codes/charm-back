package ru.andrewb.charm.back.controller.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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

import static ru.andrewb.charm.back.config.OpenApiConfig.BEARER_AUTH;
import static ru.andrewb.charm.back.web.Urls.MATCHES_REST_URL;

@Tag(name = "Matches", description = "Mutual likes")
@SecurityRequirement(name = BEARER_AUTH)
@RestController
@RequestMapping(MATCHES_REST_URL)
public class MatchesRestController {

    private final ProfileService service;

    public MatchesRestController(ProfileService service) {
        this.service = service;
    }

    @Schema(description = "Paged matches response")
    public record MatchesResponse(List<ProfileGetDto> items, boolean hasNext) {}

    @Operation(summary = "Get current user matches")
    @GetMapping
    public ResponseEntity<?> getMatches(
            @Parameter(hidden = true) @AuthenticationPrincipal AuthUser user,
            @Parameter(description = "Page number, starting from 1", example = "1")
            @RequestParam(name = "page", required = false) Integer page,
            @Parameter(description = "Items per page", example = "10")
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
