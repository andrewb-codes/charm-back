package ru.andrewb.charm.back.normalizer;

import org.junit.jupiter.api.Test;
import ru.andrewb.charm.back.dto.Action;
import ru.andrewb.charm.back.service.command.CharmCommand;

import static org.junit.jupiter.api.Assertions.*;

class CharmCommandDefaultsTest {

    @Test
    void normalize_shouldSetDefaultActionToSkip_whenActionIsNull() {
        CharmCommand dto = new CharmCommand();
        dto.setToProfileId(42L);

        CharmCommand result = CharmCommandDefaults.normalize(dto);

        assertSame(dto, result);
        assertEquals(Action.SKIP, result.getAction());
        assertNull(result.getToProfileId());
    }

    @Test
    void normalize_shouldClearToProfileId_whenActionIsSkip() {
        CharmCommand dto = new CharmCommand();
        dto.setAction(Action.SKIP);
        dto.setToProfileId(42L);

        CharmCommand result = CharmCommandDefaults.normalize(dto);

        assertEquals(Action.SKIP, result.getAction());
        assertNull(result.getToProfileId());
    }

    @Test
    void normalize_shouldKeepToProfileId_whenActionIsLike() {
        CharmCommand dto = new CharmCommand();
        dto.setAction(Action.LIKE);
        dto.setToProfileId(42L);

        CharmCommand result = CharmCommandDefaults.normalize(dto);

        assertEquals(Action.LIKE, result.getAction());
        assertEquals(42L, result.getToProfileId());
    }

    @Test
    void normalize_shouldKeepToProfileId_whenActionIsDislike() {
        CharmCommand dto = new CharmCommand();
        dto.setAction(Action.DISLIKE);
        dto.setToProfileId(42L);

        CharmCommand result = CharmCommandDefaults.normalize(dto);

        assertEquals(Action.DISLIKE, result.getAction());
        assertEquals(42L, result.getToProfileId());
    }
}
