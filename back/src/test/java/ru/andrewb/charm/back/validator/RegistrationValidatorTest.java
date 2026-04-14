package ru.andrewb.charm.back.validator;

import org.junit.jupiter.api.Test;
import ru.andrewb.charm.back.dto.RegistrationDto;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RegistrationValidatorTest {

    private final RegistrationValidator validator = new RegistrationValidator();

    @Test
    void validate_shouldReturnError_whenDtoIsNull() {
        ValidationResult result = validator.validate(null);

        assertTrue(result.isNotValid());
        assertEquals(List.of("error.dto.required"), result.getErrors());
    }

    @Test
    void validate_shouldReturnValidResult_forCorrectDto() {
        RegistrationDto dto = new RegistrationDto();
        dto.setEmail("user@mail.ru");
        dto.setPassword("123456");

        ValidationResult result = validator.validate(dto);

        assertFalse(result.isNotValid());
        assertEquals(List.of(), result.getErrors());
    }

    @Test
    void validate_shouldReturnError_whenEmailIsNull() {
        RegistrationDto dto = new RegistrationDto();
        dto.setEmail(null);
        dto.setPassword("123456");

        ValidationResult result = validator.validate(dto);

        assertTrue(result.isNotValid());
        assertEquals(List.of("error.email.required"), result.getErrors());
    }

    @Test
    void validate_shouldReturnError_whenEmailIsBlank() {
        RegistrationDto dto = new RegistrationDto();
        dto.setEmail("   ");
        dto.setPassword("123456");

        ValidationResult result = validator.validate(dto);

        assertTrue(result.isNotValid());
        assertEquals(List.of("error.email.required"), result.getErrors());
    }

    @Test
    void validate_shouldReturnError_whenEmailHasInvalidFormat() {
        RegistrationDto dto = new RegistrationDto();
        dto.setEmail("invalid");
        dto.setPassword("123456");

        ValidationResult result = validator.validate(dto);

        assertTrue(result.isNotValid());
        assertEquals(List.of("error.email.invalid"), result.getErrors());
    }

    @Test
    void validate_shouldReturnError_whenPasswordIsNull() {
        RegistrationDto dto = new RegistrationDto();
        dto.setEmail("user@mail.ru");
        dto.setPassword(null);

        ValidationResult result = validator.validate(dto);

        assertTrue(result.isNotValid());
        assertEquals(List.of("error.password.required"), result.getErrors());
    }

    @Test
    void validate_shouldReturnError_whenPasswordIsBlank() {
        RegistrationDto dto = new RegistrationDto();
        dto.setEmail("user@mail.ru");
        dto.setPassword("   ");

        ValidationResult result = validator.validate(dto);

        assertTrue(result.isNotValid());
        assertEquals(List.of("error.password.required"), result.getErrors());
    }

    @Test
    void validate_shouldReturnError_whenPasswordIsTooShort() {
        RegistrationDto dto = new RegistrationDto();
        dto.setEmail("user@mail.ru");
        dto.setPassword("123");

        ValidationResult result = validator.validate(dto);

        assertTrue(result.isNotValid());
        assertEquals(List.of("error.password.short"), result.getErrors());
    }

    @Test
    void validate_shouldReturnBothErrors_wherEmailAndPasswordAreInvalid() {
        RegistrationDto dto = new RegistrationDto();
        dto.setEmail("invalid");
        dto.setPassword("123");

        ValidationResult result = validator.validate(dto);

        assertTrue(result.isNotValid());
        assertEquals(
                List.of("error.email.invalid", "error.password.short"),
                result.getErrors()
        );
    }

    @Test
    void validate_shouldAcceptTrimmedEmailAndPassword() {
        RegistrationDto dto = new RegistrationDto();
        dto.setEmail("   user@mail.ru   ");
        dto.setPassword("   123456   ");

        ValidationResult result = validator.validate(dto);

        assertFalse(result.isNotValid());
    }
}
