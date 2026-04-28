package ru.andrewb.charm.back.controller.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import ru.andrewb.charm.back.controller.request.CharmRequest;
import ru.andrewb.charm.back.dto.ProfileSimpleDto;
import ru.andrewb.charm.back.mapper.CharmRequestToCommandMapper;
import ru.andrewb.charm.back.normalizer.CharmCommandDefaults;
import ru.andrewb.charm.back.security.AuthUser;
import ru.andrewb.charm.back.service.CharmService;
import ru.andrewb.charm.back.service.command.CharmCommand;

import static ru.andrewb.charm.back.config.OpenApiConfig.BEARER_AUTH;
import static ru.andrewb.charm.back.web.Urls.CHARM_REST_URL;

@Tag(name = "Charm", description = "Recommendations, likes, dislikes, and skips")
@SecurityRequirement(name = BEARER_AUTH)
@RestController
@RequestMapping(CHARM_REST_URL)
public class CharmRestController {

    private final CharmService service;
    private final CharmRequestToCommandMapper mapper;

    public CharmRestController(
            CharmService service,
            CharmRequestToCommandMapper mapper
    ) {
        this.service = service;
        this.mapper = mapper;
    }

    @Schema(description = "Next recommendation response. The profile is null when there are no candidates.")
    public record NextResponse(ProfileSimpleDto profile) {}

    @Operation(summary = "Get next recommendation")
    @GetMapping
    public ResponseEntity<?> getNext(@Parameter(hidden = true) @AuthenticationPrincipal AuthUser user) {
        var command = new CharmCommand();
        command.setFromProfileId(user.getId());
        CharmCommandDefaults.normalize(command);

        var nextOpt = service.getNext(command);
        return ResponseEntity.ok(new NextResponse(nextOpt.orElse(null)));
    }

    @Operation(summary = "Save charm action and get next recommendation")
    @PostMapping
    public ResponseEntity<?> postAction(
            @Parameter(hidden = true) @AuthenticationPrincipal AuthUser user,
            @Valid @RequestBody CharmRequest request
    ) {
        CharmCommand command = mapper.map(request);
        command.setFromProfileId(user.getId());
        CharmCommandDefaults.normalize(command);

        var nextOpt = service.getNext(command);
        return ResponseEntity.ok(new NextResponse(nextOpt.orElse(null)));
    }
}
