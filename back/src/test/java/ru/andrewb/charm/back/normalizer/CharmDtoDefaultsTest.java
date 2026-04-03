package ru.andrewb.charm.back.normalizer;

import org.junit.jupiter.api.Test;
import ru.andrewb.charm.back.dto.Action;
import ru.andrewb.charm.back.dto.CharmDto;

import static org.junit.jupiter.api.Assertions.*;

class CharmDtoDefaultsTest {

    @Test
    void normalize_shouldSetDefaultActionToSkip_whenActionIsNull() {
        CharmDto dto = new CharmDto();
        dto.setToProfileId(42L);

        CharmDto result = CharmDtoDefaults.normalize(dto);

        assertSame(dto, result);
        assertEquals(Action.SKIP, result.getAction());
        assertNull(result.getToProfileId());
    }

    @Test
    void normalize_shouldClearToProfileId_whenActionIsSkip() {
        CharmDto dto = new CharmDto();
        dto.setAction(Action.SKIP);
        dto.setToProfileId(42L);

        CharmDto result = CharmDtoDefaults.normalize(dto);

        assertEquals(Action.SKIP, result.getAction());
        assertNull(result.getToProfileId());
    }

    @Test
    void normalize_shouldKeepToProfileId_whenActionIsLike() {
        CharmDto dto = new CharmDto();
        dto.setAction(Action.LIKE);
        dto.setToProfileId(42L);

        CharmDto result = CharmDtoDefaults.normalize(dto);

        assertEquals(Action.LIKE, result.getAction());
        assertEquals(42L, result.getToProfileId());
    }

    @Test
    void normalize_shouldKeepToProfileId_whenActionIsDislike() {
        CharmDto dto = new CharmDto();
        dto.setAction(Action.DISLIKE);
        dto.setToProfileId(42L);

        CharmDto result = CharmDtoDefaults.normalize(dto);

        assertEquals(Action.DISLIKE, result.getAction());
        assertEquals(42L, result.getToProfileId());
    }
}
