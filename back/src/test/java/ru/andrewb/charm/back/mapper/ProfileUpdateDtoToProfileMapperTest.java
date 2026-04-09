package ru.andrewb.charm.back.mapper;

import org.junit.jupiter.api.Test;
import ru.andrewb.charm.back.dto.ProfileUpdateDto;
import ru.andrewb.charm.back.model.Gender;
import ru.andrewb.charm.back.model.Profile;
import ru.andrewb.charm.back.model.Status;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

class ProfileUpdateDtoToProfileMapperTest {

    private final ProfileUpdateDtoToProfileMapper mapper = new ProfileUpdateDtoToProfileMapper();

    @Test
    void map_shouldCopyAllNonNullFieldsToNewProfile() {
        ProfileUpdateDto dto = new ProfileUpdateDto();
        dto.setVersion(1);
        dto.setName("Ivan");
        dto.setSurname("Ivanov");
        dto.setBirthdate(LocalDate.of(2000, 1, 1));
        dto.setAbout("About");
        dto.setGender(Gender.MALE);

        Profile profile = mapper.map(dto);

        assertEquals(1, profile.getVersion());
        assertEquals("Ivan", profile.getName());
        assertEquals("Ivanov", profile.getSurname());
        assertEquals(LocalDate.of(2000, 1, 1), profile.getBirthdate());
        assertEquals("About", profile.getAbout());
        assertEquals(Gender.MALE, profile.getGender());
    }

    @Test
    void map_shouldUpdateOnlyNonNullFieldsInExistingProfile() {
        ProfileUpdateDto dto = new ProfileUpdateDto();
        dto.setName("New name");
        dto.setAbout("New about");

        Profile profile = new Profile();
        profile.setVersion(1);
        profile.setName("Old name");
        profile.setSurname("Old surname");
        profile.setBirthdate(LocalDate.of(1999, 2, 2));
        profile.setAbout("Old about");
        profile.setGender(Gender.FEMALE);

        Profile result = mapper.map(dto, profile);

        assertSame(profile, result);
        assertEquals(1, profile.getVersion());
        assertEquals("New name", profile.getName());
        assertEquals("Old surname", profile.getSurname());
        assertEquals(LocalDate.of(1999, 2, 2), profile.getBirthdate());
        assertEquals("New about", profile.getAbout());
        assertEquals(Gender.FEMALE, profile.getGender());
    }

    @Test
    void map_shouldUpdateVersionWhenProvided() {
        ProfileUpdateDto dto = new ProfileUpdateDto();
        dto.setVersion(99);

        Profile profile = new Profile();
        profile.setVersion(1);

        mapper.map(dto, profile);

        assertEquals(99, profile.getVersion());
    }

    @Test
    void map_shouldNotOverwriteFieldsWithNulls() {
        ProfileUpdateDto dto = new ProfileUpdateDto();

        Profile profile = new Profile();
        profile.setVersion(5);
        profile.setName("Ivan");
        profile.setSurname("Ivanov");
        profile.setBirthdate(LocalDate.of(2001, 3, 4));
        profile.setAbout("About");
        profile.setGender(Gender.MALE);

        mapper.map(dto, profile);

        assertEquals(5, profile.getVersion());
        assertEquals("Ivan", profile.getName());
        assertEquals("Ivanov", profile.getSurname());
        assertEquals(LocalDate.of(2001, 3, 4), profile.getBirthdate());
        assertEquals("About", profile.getAbout());
        assertEquals(Gender.MALE, profile.getGender());
    }
}
