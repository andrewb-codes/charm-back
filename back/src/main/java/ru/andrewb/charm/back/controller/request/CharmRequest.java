package ru.andrewb.charm.back.controller.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.FieldDefaults;
import ru.andrewb.charm.back.dto.Action;

@Getter
@Setter
@ToString
@Schema(description = "Charm action request")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CharmRequest {
    @Schema(description = "Target profile id", example = "2")
    Long toProfileId;

    @Schema(description = "Action for the target profile. SKIP does not persist a like/dislike.", example = "LIKE")
    Action action;
}
