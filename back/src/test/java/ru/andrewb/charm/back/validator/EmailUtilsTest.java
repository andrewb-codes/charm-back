package ru.andrewb.charm.back.validator;

import org.junit.jupiter.api.Test;
import ru.andrewb.charm.back.model.exception.BadRequestException;

import static org.junit.jupiter.api.Assertions.*;

class EmailUtilsTest {

    @Test
    void normalize_shouldReturnNull_forNull() {
        assertNull(EmailUtils.normalize(null));
    }

    @Test
    void normalize_shouldTrimValue() {
        assertEquals("user@mail.com", EmailUtils.normalize("  user@mail.com  "));
    }

    @Test
    void hasText_shouldReturnFalse_forNull() {
        assertFalse(EmailUtils.hasText(null));
    }

    @Test
    void hasText_shouldReturnFalse_forEmptyString() {
        assertFalse(EmailUtils.hasText(""));
    }

    @Test
    void hasText_shouldReturnFalse_forBlankString() {
        assertFalse(EmailUtils.hasText("   "));
    }

    @Test
    void hasText_shouldReturnTrue_forNonBlankString() {
        assertTrue(EmailUtils.hasText("user@mail.com"));
    }

    @Test
    void matchesFormat_shouldReturnTrue_forValidEmail() {
        assertTrue(EmailUtils.matchesFormat("user@mail.com"));
    }

    @Test
    void matchesFormat_shouldReturnTrue_forUppercaseEmail() {
        assertTrue(EmailUtils.matchesFormat("USER@MAIL.COM"));
    }

    @Test
    void matchesFormat_shouldReturnTrue_forEmailWithPlus() {
        assertTrue(EmailUtils.matchesFormat("user+tag@mail.com"));
    }

    @Test
    void matchesFormat_shouldReturnFalse_forNull() {
        assertFalse(EmailUtils.matchesFormat(null));
    }

    @Test
    void matchesFormat_shouldReturnFalse_whenMissingAtSign() {
        assertFalse(EmailUtils.matchesFormat("usermail.com"));
    }

    @Test
    void matchesFormat_shouldReturnFalse_whenMissingDomain() {
        assertFalse(EmailUtils.matchesFormat("user@"));
    }

    @Test
    void requireValidOrThrow_shouldThrow_whenEmailIsNull() {
        BadRequestException ex = assertThrows(
                BadRequestException.class,
                () -> EmailUtils.requireValidOrThrow(null)
        );

        assertEquals("error.email.required", ex.getMessage());
    }

    @Test
    void requireValidOrThrow_shouldThrow_whenEmailIsBlank() {
        BadRequestException ex = assertThrows(
                BadRequestException.class,
                () -> EmailUtils.requireValidOrThrow("   ")
        );

        assertEquals("error.email.required", ex.getMessage());
    }

    @Test
    void requireValidOrThrow_shouldThrow_whenEmailHasInvalidFormat() {
        BadRequestException ex = assertThrows(
                BadRequestException.class,
                () -> EmailUtils.requireValidOrThrow("invalid")
        );

        assertEquals("error.email.invalid", ex.getMessage());
    }


}
