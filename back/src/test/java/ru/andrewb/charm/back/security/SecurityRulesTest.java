package ru.andrewb.charm.back.security;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SecurityRulesTest {

    @Test
    void isPublicUi_shouldReturnTrueForLogin() {
        assertTrue(SecurityRules.isPublicUi("/login"));
    }

    @Test
    void isPublicUi_shouldReturnFalseForProfile() {
        assertFalse(SecurityRules.isPublicUi("/profile"));
    }

    @Test
    void isPublicUi_shouldReturnTrue_forStaticAsset() {
        assertTrue(SecurityRules.isPublicUi("/img/logo.png"));
    }

    @Test
    void isRest_shouldReturnTrue_forApiPath() {
        assertTrue(SecurityRules.isRest("/api/v1/profile"));
    }

    @Test
    void isRest_shouldReturnFalse_forUiPath() {
        assertFalse(SecurityRules.isRest("/profile"));
    }

    @Test
    void isSafeInternalRedirect_shouldAllowSafeInternalPath() {
        boolean result = SecurityRules.isSafeInternalRedirect(
                "/app",
                "/app/profiles?page=2",
                "/profiles"
        );

        assertTrue(result);
    }

    @Test
    void isSafeInternalRedirect_shouldRejectAbsoluteUrl() {
        boolean result = SecurityRules.isSafeInternalRedirect(
                "/app",
                "https://evil.com/app/profiles",
                "/profiles"
        );

        assertFalse(result);
    }

    @Test
    void isSafeInternalRedirect_shouldRejectPathOutsideContext() {
        boolean result = SecurityRules.isSafeInternalRedirect(
                "/app",
                "/other/profiles",
                "/profiles"
        );

        assertFalse(result);
    }

    @Test
    void isSafeInternalRedirect_shouldRejectPathTraversal() {
        boolean result = SecurityRules.isSafeInternalRedirect(
                "/app",
                "/app/../admin",
                "/profiles"
        );

        assertFalse(result);
    }

    @Test
    void isSafeInternalRedirect_shouldRejectCrLfInjection() {
        boolean result = SecurityRules.isSafeInternalRedirect(
                "/app",
                "/app/profiles%0d%0aLocation:https://evil.com",
                "/profiles"
        );

        assertFalse(result);
    }

    @Test
    void isSafeInternalRedirect_shouldRejectNotAllowedPrefix() {
        boolean result = SecurityRules.isSafeInternalRedirect(
                "/app",
                "/app/settings",
                "/profiles"
        );

        assertFalse(result);
    }
}
