package ru.andrewb.charm.back.validator;

import org.junit.jupiter.api.Test;
import ru.andrewb.charm.back.dto.PasswordChangeDto;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PasswordChangeValidatorTest {

    private final PasswordChangeValidator validator = PasswordChangeValidator.getInstance();

    @Test
    void validate_shouldReturnError_whenDtoIsNull() {
        ValidationResult result = validator.validate(null);

        assertTrue(result.isNotValid());
        assertEquals(List.of("error.dto.required"), result.getErrors());
    }

    @Test
    void validate_shouldReturnValidResult_forCorrectDto() {
        PasswordChangeDto dto = new PasswordChangeDto();
        dto.setCurrentPassword("oldpass");
        dto.setNewPassword("newpass");
        dto.setConfirmPassword("newpass");

        ValidationResult result = validator.validate(dto);

        assertFalse(result.isNotValid());
        assertEquals(List.of(), result.getErrors());
    }

    @Test
    void validate_shouldReturnError_whenCurrentPasswordIsMissing() {
        PasswordChangeDto dto = new PasswordChangeDto();
        dto.setCurrentPassword("   ");
        dto.setNewPassword("newpass");
        dto.setConfirmPassword("newpass");

        ValidationResult result = validator.validate(dto);

        assertTrue(result.isNotValid());
        assertEquals(List.of("error.password.current-required"), result.getErrors());
    }

    @Test
    void validate_shouldReturnBothErrors_whenNewPasswordIsMissing() {
        PasswordChangeDto dto = new PasswordChangeDto();
        dto.setCurrentPassword("oldpass");
        dto.setNewPassword("   ");
        dto.setConfirmPassword("newpass");

        ValidationResult result = validator.validate(dto);

        assertTrue(result.isNotValid());
        assertEquals(
                List.of(
                        "error.password.new-required",
                        "error.password.mismatch"
                ),
                result.getErrors()
        );
    }

    @Test
    void validate_shouldReturnError_whenNewPasswordIsTooShort() {
        PasswordChangeDto dto = new PasswordChangeDto();
        dto.setCurrentPassword("oldpass");
        dto.setNewPassword("123");
        dto.setConfirmPassword("123");

        ValidationResult result = validator.validate(dto);

        assertTrue(result.isNotValid());
        assertEquals(List.of("error.password.short"), result.getErrors());
    }

    @Test
    void validate_shouldReturnError_whenConfirmPasswordIsMissing() {
        PasswordChangeDto dto = new PasswordChangeDto();
        dto.setCurrentPassword("oldpass");
        dto.setNewPassword("newpass");
        dto.setConfirmPassword("   ");

        ValidationResult result = validator.validate(dto);

        assertTrue(result.isNotValid());
        assertEquals(List.of("error.password.confirm-required"), result.getErrors());
    }

    @Test
    void validate_shouldReturnError_whenConfirmPasswordDoesNotMatch() {
        PasswordChangeDto dto = new PasswordChangeDto();
        dto.setCurrentPassword("oldpass");
        dto.setNewPassword("newpass");
        dto.setConfirmPassword("otherpass");

        ValidationResult result = validator.validate(dto);

        assertTrue(result.isNotValid());
        assertEquals(List.of("error.password.mismatch"), result.getErrors());
    }

    @Test
    void validate_shouldReturnMultipleErrors_whenSeveralFieldsAreInvalid() {
        PasswordChangeDto dto = new PasswordChangeDto();
        dto.setCurrentPassword("   ");
        dto.setNewPassword("123");
        dto.setConfirmPassword("456");

        ValidationResult result = validator.validate(dto);

        assertTrue(result.isNotValid());
        assertEquals(
                List.of(
                        "error.password.current-required",
                        "error.password.short",
                        "error.password.mismatch"
                ),
                result.getErrors()
        );
    }

    @Test
    void validate_shouldAcceptTrimmedValues() {
        PasswordChangeDto dto = new PasswordChangeDto();
        dto.setCurrentPassword("  oldpass  ");
        dto.setNewPassword(  "newpass"  );
        dto.setConfirmPassword("  newpass  ");

        ValidationResult result = validator.validate(dto);

        assertFalse(result.isNotValid());
    }
}
