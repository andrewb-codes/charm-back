package ru.andrewb.charm.back.controller.rest;

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

import static ru.andrewb.charm.back.web.Urls.CHARM_REST_URL;

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

    public record NextResponse(ProfileSimpleDto profile) {}

    @GetMapping
    public ResponseEntity<?> getNext(@AuthenticationPrincipal AuthUser user) {
        var command = new CharmCommand();
        command.setFromProfileId(user.getId());
        CharmCommandDefaults.normalize(command);

        var nextOpt = service.getNext(command);
        return ResponseEntity.ok(new NextResponse(nextOpt.orElse(null)));
    }

    @PostMapping
    public ResponseEntity<?> postAction(
            @AuthenticationPrincipal AuthUser user,
            @Valid @RequestBody CharmRequest request
    ) {
        CharmCommand command = mapper.map(request);
        command.setFromProfileId(user.getId());
        CharmCommandDefaults.normalize(command);

        var nextOpt = service.getNext(command);
        return ResponseEntity.ok(new NextResponse(nextOpt.orElse(null)));
    }
}
