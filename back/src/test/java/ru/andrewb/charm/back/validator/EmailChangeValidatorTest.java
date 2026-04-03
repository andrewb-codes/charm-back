package ru.andrewb.charm.back.validator;

import org.junit.jupiter.api.Test;
import ru.andrewb.charm.back.dto.EmailChangeDto;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class EmailChangeValidatorTest {

    private final EmailChangeValidator validator = EmailChangeValidator.getInstance();

    @Test
    void validate_shouldReturnError_whenDtoIsNull() {
        ValidationResult result = validator.validate(null);

        assertTrue(result.isNotValid());
        assertEquals(List.of("error.dto.required"), result.getErrors());
    }

    @Test
    void validate_shouldReturnValidResult_forCorrectDto() {
        EmailChangeDto dto = new EmailChangeDto();
        dto.setNewEmail("user@mail.com");
        dto.setCurrentPassword("123456");

        ValidationResult result = validator.validate(dto);

        assertFalse(result.isNotValid());
        assertEquals(List.of(), result.getErrors());
    }

    @Test
    void validate_shouldReturnError_whenNewEmailIsNull() {
        EmailChangeDto dto = new EmailChangeDto();
        dto.setNewEmail(null);
        dto.setCurrentPassword("123456");

        ValidationResult result = validator.validate(dto);

        assertTrue(result.isNotValid());
        assertEquals(List.of("error.email.required"), result.getErrors());
    }

    @Test
    void validate_shouldReturnError_whenNewEmailIsBlank() {
        EmailChangeDto dto = new EmailChangeDto();
        dto.setNewEmail("   ");
        dto.setCurrentPassword("123456");

        ValidationResult result = validator.validate(dto);

        assertTrue(result.isNotValid());
        assertEquals(List.of("error.email.required"), result.getErrors());
    }

    @Test
    void validate_shouldReturnError_whenNewEmailHasInvalidFormat() {
        EmailChangeDto dto = new EmailChangeDto();
        dto.setNewEmail("invalid");
        dto.setCurrentPassword("123456");

        ValidationResult result = validator.validate(dto);

        assertTrue(result.isNotValid());
        assertEquals(List.of("error.email.invalid"), result.getErrors());
    }

    @Test
    void validate_shouldReturnError_whenCurrentPasswordIsNull() {
        EmailChangeDto dto = new EmailChangeDto();
        dto.setNewEmail("user@mail.com");
        dto.setCurrentPassword(null);

        ValidationResult result = validator.validate(dto);

        assertTrue(result.isNotValid());
        assertEquals(List.of("error.password.current-required"), result.getErrors());
    }

    @Test
    void validate_shouldReturnError_whenCurrentPasswordIsBlank() {
        EmailChangeDto dto = new EmailChangeDto();
        dto.setNewEmail("user@mail.com");
        dto.setCurrentPassword("   ");

        ValidationResult result = validator.validate(dto);

        assertTrue(result.isNotValid());
        assertEquals(List.of("error.password.current-required"), result.getErrors());
    }

    @Test
    void validate_shouldReturnBothErrors_whenEmailAndPasswordAreInvalid() {
        EmailChangeDto dto = new EmailChangeDto();
        dto.setNewEmail("invalid");
        dto.setCurrentPassword("   ");

        ValidationResult result = validator.validate(dto);

        assertTrue(result.isNotValid());
        assertEquals(
                List.of("error.email.invalid", "error.password.current-required"),
                result.getErrors()
        );
    }

    @Test
    void validate_shouldAcceptTrimmedValues() {
        EmailChangeDto dto = new EmailChangeDto();
        dto.setNewEmail("  user@mail.com  ");
        dto.setCurrentPassword("  123456  ");

        ValidationResult result = validator.validate(dto);

        assertFalse(result.isNotValid());
    }
}
