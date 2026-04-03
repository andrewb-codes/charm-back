package ru.andrewb.charm.back.mapper;

import org.junit.jupiter.api.Test;
import ru.andrewb.charm.back.dto.ProfileGetDto;
import ru.andrewb.charm.back.model.Gender;
import ru.andrewb.charm.back.model.Profile;
import ru.andrewb.charm.back.model.Role;
import ru.andrewb.charm.back.model.Status;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.*;

class ProfileToProfileGetDtoMapperTest {

    private final ProfileToProfileGetDtoMapper mapper = ProfileToProfileGetDtoMapper.getInstance();

    @Test
    void map_shouldCopyAllFieldsAndCalculateAge() {
        LocalDate birthdate = LocalDate.now().minusYears(25).plusDays(1);

        Profile profile = new Profile();
        profile.setId(1L);
        profile.setVersion(7);
        profile.setEmail("user@mail.com");
        profile.setName("Ivan");
        profile.setSurname("Ivanov");
        profile.setAbout("About");
        profile.setBirthdate(birthdate);
        profile.setGender(Gender.MALE);
        profile.setStatus(Status.ACTIVE);
        profile.setPhoto("photo.jpg");
        profile.setRole(Role.USER);

        ProfileGetDto dto = mapper.map(profile);

        assertEquals(1L, dto.getId());
        assertEquals(7, dto.getVersion());
        assertEquals("user@mail.com", dto.getEmail());
        assertEquals("Ivan", dto.getName());
        assertEquals("Ivanov", dto.getSurname());
        assertEquals("About", dto.getAbout());
        assertEquals(birthdate, dto.getBirthdate());
        assertEquals(
                Math.toIntExact(ChronoUnit.YEARS.between(birthdate, LocalDate.now())),
                dto.getAge()
        );
        assertEquals(Gender.MALE, dto.getGender());
        assertEquals(Status.ACTIVE, dto.getStatus());
        assertEquals("photo.jpg", dto.getPhoto());
        assertEquals(Role.USER, dto.getRole());
    }

    @Test
    void map_shouldSetAgeToNull_whenBirthdateIsNull() {
        Profile  profile = new Profile();
        profile.setId(1L);
        profile.setBirthdate(null);

        ProfileGetDto dto = mapper.map(profile);

        assertEquals(1L, dto.getId());
        assertNull(dto.getBirthdate());
        assertNull(dto.getAge());
    }

    @Test
    void map_shouldFillProvidedTargetDto() {
        Profile profile = new Profile();
        profile.setId(10L);
        profile.setEmail("new@mail.com");
        profile.setName("Ivan");
        profile.setSurname("Ivanov");
        profile.setRole(Role.ADMIN);

        ProfileGetDto target = new ProfileGetDto();
        target.setEmail("old@mail.com");

        ProfileGetDto result = mapper.map(profile, target);

        assertSame(target, result);
        assertEquals(10L, target.getId());
        assertEquals("new@mail.com", target.getEmail());
        assertEquals("Ivan", target.getName());
        assertEquals("Ivanov", target.getSurname());
        assertEquals(Role.ADMIN, target.getRole());
    }
}
