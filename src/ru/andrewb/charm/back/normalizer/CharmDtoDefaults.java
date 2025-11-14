package ru.andrewb.charm.back.normalizer;

import ru.andrewb.charm.back.dto.Action;
import ru.andrewb.charm.back.dto.CharmDto;

public class CharmDtoDefaults {

    public static final Action DEFAULT_ACTION = Action.SKIP;

    public static CharmDto normalize(CharmDto dto) {
        if (dto.getAction() == null) {
            dto.setAction(DEFAULT_ACTION);
        }
   
        if (dto.getAction() == Action.SKIP) {
            dto.setToProfileId(null);
        }
        return dto;
    }
}
