package ru.andrewb.charm.back.normalizer;

import lombok.experimental.UtilityClass;
import ru.andrewb.charm.back.dto.Action;
import ru.andrewb.charm.back.service.command.CharmCommand;

@UtilityClass
public class CharmCommandDefaults {

    public static final Action DEFAULT_ACTION = Action.SKIP;

    public static CharmCommand normalize(CharmCommand command) {
        if (command.getAction() == null) {
            command.setAction(DEFAULT_ACTION);
        }

        if (command.getAction() == Action.SKIP) {
            command.setToProfileId(null);
        }
        return command;
    }
}
