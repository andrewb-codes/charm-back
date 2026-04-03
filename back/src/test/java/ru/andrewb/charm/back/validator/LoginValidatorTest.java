package ru.andrewb.charm.back.validator;

import org.junit.jupiter.api.Test;
import ru.andrewb.charm.back.dto.LoginDto;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class LoginValidatorTest {

    private final LoginValidator validator = LoginValidator.getInstance();

    @Test
    void validate_shouldReturnError_whenDtoIsNull() {
        ValidationResult result = validator.validate(null);

        assertTrue(result.isNotValid());
        assertEquals(List.of("error.dto.required"), result.getErrors());
    }

    @Test
    void validate_shouldReturnValidResult_forCorrectDto() {
        LoginDto dto = new LoginDto();
        dto.setEmail("user@mail.ru");
        dto.setPassword("123456");

        ValidationResult result = validator.validate(dto);

        assertFalse(result.isNotValid());
        assertEquals(List.of(), result.getErrors());
    }

    @Test
    void validate_shouldReturnError_whenEmailIsNull() {
        LoginDto dto = new LoginDto();
        dto.setEmail(null);
        dto.setPassword("123456");

        ValidationResult result = validator.validate(dto);

        assertTrue(result.isNotValid());
        assertEquals(List.of("error.email.required"), result.getErrors());
    }

    @Test
    void validate_shouldReturnError_whenEmailIsBlank() {
        LoginDto dto = new LoginDto();
        dto.setEmail("   ");
        dto.setPassword("123456");

        ValidationResult result = validator.validate(dto);

        assertTrue(result.isNotValid());
        assertEquals(List.of("error.email.required"), result.getErrors());
    }

    @Test
    void validate_shouldReturnError_whenEmailHasInvalidFormat() {
        LoginDto dto = new LoginDto();
        dto.setEmail("invalid");
        dto.setPassword("123456");

        ValidationResult result = validator.validate(dto);

        assertTrue(result.isNotValid());
        assertEquals(List.of("error.email.invalid"), result.getErrors());
    }

    @Test
    void validate_shouldReturnError_whenPasswordIsNull() {
        LoginDto dto = new LoginDto();
        dto.setEmail("user@mail.ru");
        dto.setPassword(null);

        ValidationResult result = validator.validate(dto);

        assertTrue(result.isNotValid());
        assertEquals(List.of("error.password.required"), result.getErrors());
    }

    @Test
    void validate_shouldReturnError_whenPasswordIsBlank() {
        LoginDto dto = new LoginDto();
        dto.setEmail("user@mail.ru");
        dto.setPassword("   ");

        ValidationResult result = validator.validate(dto);

        assertTrue(result.isNotValid());
        assertEquals(List.of("error.password.required"), result.getErrors());
    }

    @Test
    void validate_shouldReturnBothErrors_wherEmailAndPasswordAreInvalid() {
        LoginDto dto = new LoginDto();
        dto.setEmail("invalid");
        dto.setPassword("   ");

        ValidationResult result = validator.validate(dto);

        assertTrue(result.isNotValid());
        assertEquals(
                List.of("error.email.invalid", "error.password.required"),
                result.getErrors()
        );
    }

    @Test
    void validate_shouldAcceptTrimmedEmailAndPassword() {
        LoginDto dto = new LoginDto();
        dto.setEmail("   user@mail.ru   ");
        dto.setPassword("   123456   ");

        ValidationResult result = validator.validate(dto);

        assertFalse(result.isNotValid());
    }

}
