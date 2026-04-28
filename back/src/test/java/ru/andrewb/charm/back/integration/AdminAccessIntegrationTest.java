package ru.andrewb.charm.back.integration;

import org.junit.jupiter.api.Test;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AdminAccessIntegrationTest extends AbstractWebIntegrationTest {

    @Test
    void adminProfilesEndpoint_shouldReturnProfiles_forAdmin() throws Exception {
        String adminToken = loginAndGetToken("admin@charm.ru", "qwerty");

        mockMvc.perform(
                        get("/api/v1/admin/profiles")
                                .header("Authorization", "Bearer " + adminToken)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").isNumber());
    }

    @Test
    void adminProfilesEndpoint_shouldReturn403_forRegularUser() throws Exception {
        String email = uniqueEmail();
        registerUser(email, "123456");
        String userToken = loginAndGetToken(email, "123456");

        mockMvc.perform(
                        get("/api/v1/admin/profiles")
                                .header("Authorization", "Bearer " + userToken)
                )
                .andExpect(status().isForbidden());
    }
}
