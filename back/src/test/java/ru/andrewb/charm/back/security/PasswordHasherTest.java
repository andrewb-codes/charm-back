package ru.andrewb.charm.back.security;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PasswordHasherTest {

    @Test
    void hashPwd_shouldCreateHashDifferentFromSource() {
        String raw = "password";
        String hash = PasswordHasher.hashPwd(raw);

        assertNotNull(hash);
        assertNotEquals(raw, hash);
    }

    @Test
    void checkPwd_shouldReturnTrue_forMatchingPassword() {
        String raw = "password";
        String hash = PasswordHasher.hashPwd(raw);

        assertTrue(PasswordHasher.checkPwd(raw, hash));
    }

    @Test
    void checkPwd_shouldReturnFalse_forNonMatchingPassword() {
        String wrongRaw = "wrong";
        String correctHash = PasswordHasher.hashPwd("correct");

        assertFalse(PasswordHasher.checkPwd(wrongRaw, correctHash));
    }
}
