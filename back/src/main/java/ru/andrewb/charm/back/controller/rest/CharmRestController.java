package ru.andrewb.charm.back.controller.rest;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import ru.andrewb.charm.back.dto.CharmDto;
import ru.andrewb.charm.back.dto.ProfileSimpleDto;
import ru.andrewb.charm.back.normalizer.CharmDtoDefaults;
import ru.andrewb.charm.back.security.AuthUser;
import ru.andrewb.charm.back.service.CharmService;

import static ru.andrewb.charm.back.web.Urls.CHARM_REST_URL;

@RestController
@RequestMapping(CHARM_REST_URL)
public class CharmRestController {

    private final CharmService service;

    public CharmRestController(CharmService service) {
        this.service = service;
    }

    public record NextResponse(ProfileSimpleDto profile) {}

    @GetMapping
    public ResponseEntity<?> getNext(@AuthenticationPrincipal AuthUser user) {
        var dto = new CharmDto();
        dto.setFromProfileId(user.getId());
        CharmDtoDefaults.normalize(dto);

        var nextOpt = service.getNext(dto);
        return ResponseEntity.ok(new NextResponse(nextOpt.orElse(null)));
    }

    @PostMapping
    public ResponseEntity<?> postAction(
            @AuthenticationPrincipal AuthUser user,
            @RequestBody CharmDto dto
    ) {
        dto.setFromProfileId(user.getId());
        CharmDtoDefaults.normalize(dto);

        var nextOpt = service.getNext(dto);
        return ResponseEntity.ok(new NextResponse(nextOpt.orElse(null)));
    }
}
