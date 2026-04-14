package ru.andrewb.charm.back.validator;

import org.junit.jupiter.api.Test;
import ru.andrewb.charm.back.dto.ProfileUpdateDto;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ProfileUpdateValidatorTest {

    private final ProfileUpdateValidator validator = new ProfileUpdateValidator();

    @Test
    void validate_shouldReturnError_whenDtoIsNull() {
        ValidationResult result = validator.validate(null);

        assertTrue(result.isNotValid());
        assertEquals(List.of("error.dto.required"), result.getErrors());
    }

    @Test
    void validate_shouldReturnValidResult_forCorrectDto() {
        ProfileUpdateDto dto = new ProfileUpdateDto();
        dto.setName("Ivan");
        dto.setSurname("Ivanov");
        dto.setAbout("About me");
        dto.setBirthdate(LocalDate.now().minusYears(20));

        ValidationResult result = validator.validate(dto);

        assertFalse(result.isNotValid());
        assertEquals(List.of(), result.getErrors());
    }

    @Test
    void validate_shouldReturnError_whenNameIsTooLong() {
        ProfileUpdateDto dto = new ProfileUpdateDto();
        dto.setName("a".repeat(101));

        ValidationResult result = validator.validate(dto);

        assertTrue(result.isNotValid());
        assertEquals(List.of("error.name.too-long"), result.getErrors());
    }

    @Test
    void validate_shouldReturnError_whenSurnameIsTooLong() {
        ProfileUpdateDto dto = new ProfileUpdateDto();
        dto.setSurname("a".repeat(101));

        ValidationResult result = validator.validate(dto);

        assertTrue(result.isNotValid());
        assertEquals(List.of("error.surname.too-long"), result.getErrors());
    }

    @Test
    void validate_shouldReturnError_whenAboutIsTooLong() {
        ProfileUpdateDto dto = new ProfileUpdateDto();
        dto.setAbout("a".repeat(1001));

        ValidationResult result = validator.validate(dto);

        assertTrue(result.isNotValid());
        assertEquals(List.of("error.about.too-long"), result.getErrors());
    }

    @Test
    void validate_shouldReturnError_whenBirthdateIsInFuture() {
        ProfileUpdateDto dto = new ProfileUpdateDto();
        dto.setBirthdate(LocalDate.now().plusDays(1));

        ValidationResult result = validator.validate(dto);

        assertTrue(result.isNotValid());
        assertEquals(
                List.of(
                        "error.birthdate.future",
                        "error.birthdate.underage"
                ),
                result.getErrors()
        );
    }

    @Test
    void validate_shouldReturnError_whenBirthdateIsTooOld() {
        ProfileUpdateDto dto = new ProfileUpdateDto();
        dto.setBirthdate(LocalDate.of(1899, 12, 31));

        ValidationResult result = validator.validate(dto);

        assertTrue(result.isNotValid());
        assertEquals(List.of("error.birthdate.too-old"), result.getErrors());
    }

    @Test
    void validate_shouldReturnError_whenUserIsUnderage() {
        ProfileUpdateDto dto = new ProfileUpdateDto();
        dto.setBirthdate(LocalDate.now().minusYears(17));

        ValidationResult result = validator.validate(dto);

        assertTrue(result.isNotValid());
        assertEquals(List.of("error.birthdate.underage"), result.getErrors());
    }

    @Test
    void validate_shouldReturnMultipleErrors_whenSeveralFieldsAreInvalid() {
        ProfileUpdateDto dto = new ProfileUpdateDto();
        dto.setName("a".repeat(101));
        dto.setSurname("b".repeat(101));
        dto.setAbout("c".repeat(1001));

        ValidationResult result = validator.validate(dto);

        assertTrue(result.isNotValid());
        assertEquals(
                List.of(
                        "error.name.too-long",
                        "error.surname.too-long",
                        "error.about.too-long"
                ),
                result.getErrors()
        );
    }
}
