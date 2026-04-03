package ru.andrewb.charm.back.validator;

import org.junit.jupiter.api.Test;
import ru.andrewb.charm.back.model.exception.BadRequestException;

import static org.junit.jupiter.api.Assertions.*;

class PasswordUtilsTest {

    @Test
    void normalize_shouldReturnNull_forNull() {
        assertNull(PasswordUtils.normalize(null));
    }

    @Test
    void normalize_shouldTrimValue() {
        assertEquals("password", PasswordUtils.normalize("  password  "));
    }

    @Test
    void hasText_shouldReturnFalse_forNull() {
        assertFalse(PasswordUtils.hasText(null));
    }

    @Test
    void hasText_shouldReturnFalse_forEmptyString() {
        assertFalse(PasswordUtils.hasText(""));
    }

    @Test
    void hasText_shouldReturnFalse_forBlankString() {
        assertFalse(PasswordUtils.hasText("   "));
    }

    @Test
    void hasText_shouldReturnTrue_forNotBlankString() {
        assertTrue(PasswordUtils.hasText("password"));
    }

    @Test
    void lengthAtLeast_shouldReturnFalse_forNull() {
        assertFalse(PasswordUtils.lengthAtLeast(null, 6));
    }

    @Test
    void lengthAtLeast_shouldReturnFalse_whenPasswordIsShorterThenMinLength() {
        assertFalse(PasswordUtils.lengthAtLeast("12345", 6));
    }

    @Test
    void lengthAtLeast_shouldReturnTrue_whenPasswordMatchesMinLength() {
        assertTrue(PasswordUtils.lengthAtLeast("123456", 6));
    }

    @Test
    void lengthAtLeast_shouldReturnTrue_whenPasswordIsLongerThenMinLength() {
        assertTrue(PasswordUtils.lengthAtLeast("123456789", 6));
    }

    @Test
    void requireValidOrThrow_shouldReturnNormalizesPassword_forValidValue() {
        String result = PasswordUtils.requireValidOrThrow("  password123   ", 6);

        assertEquals("password123", result);
    }

    @Test
    void requireValidOrThrow_shouldThrow_whenPasswordIsBlank() {
        BadRequestException ex = assertThrows(
                BadRequestException.class,
                () -> PasswordUtils.requireValidOrThrow("  ", 6)
        );

        assertEquals("error.password.required",  ex.getMessage());
    }

    @Test
    void requireValidOrThrow_shouldThrow_whenPasswordIsTooShort() {
        BadRequestException ex = assertThrows(
                BadRequestException.class,
                () -> PasswordUtils.requireValidOrThrow("12345", 6)
        );

        assertEquals("error.password.short",  ex.getMessage());
    }
}
