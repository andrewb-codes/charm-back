package ru.andrewb.charm.back.mapper;

import org.junit.jupiter.api.Test;
import ru.andrewb.charm.back.dto.UserDetailsDto;
import ru.andrewb.charm.back.model.Profile;
import ru.andrewb.charm.back.model.Role;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

class ProfileToUserDetailsDtoMapperTest {

    private final ProfileToUserDetailsDtoMapper mapper = new ProfileToUserDetailsDtoMapper();

    @Test
    void map_shouldCopyIdEmailRole() {
        Profile profile = new Profile();
        profile.setId(1L);
        profile.setEmail("admin@mail.com");
        profile.setRole(Role.ADMIN);

        UserDetailsDto dto = mapper.map(profile);

        assertEquals(1L, dto.getId());
        assertEquals("admin@mail.com", dto.getEmail());
        assertEquals(Role.ADMIN, dto.getRole());
    }

    @Test
    void map_shouldFillProvidedTargetDto() {
        Profile profile = new Profile();
        profile.setId(12L);
        profile.setEmail("user@mail.com");
        profile.setRole(Role.USER);

        UserDetailsDto target = new UserDetailsDto();
        target.setEmail("old@mail.com");

        UserDetailsDto result = mapper.map(profile, target);

        assertSame(target, result);
        assertEquals(12L, target.getId());
        assertEquals("user@mail.com", target.getEmail());
        assertEquals(Role.USER, target.getRole());
    }

    @Test
    void map_shouldOverwritePreviousValuesInTargetDto() {
        Profile profile = new Profile();
        profile.setId(99L);
        profile.setEmail("new@mail.com");
        profile.setRole(Role.USER);

        UserDetailsDto target = new UserDetailsDto();
        target.setId(1L);
        target.setEmail("old@mail.com");
        target.setRole(Role.ADMIN);

        mapper.map(profile, target);

        assertEquals(99L, target.getId());
        assertEquals("new@mail.com", target.getEmail());
        assertEquals(Role.USER, target.getRole());
    }
}
