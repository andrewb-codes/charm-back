package ru.andrewb.charm.back.service.command;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.FieldDefaults;
import ru.andrewb.charm.back.dto.Action;

@Getter
@Setter
@ToString
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CharmCommand {

    Long fromProfileId;
    Long toProfileId;
    Action action;
}
