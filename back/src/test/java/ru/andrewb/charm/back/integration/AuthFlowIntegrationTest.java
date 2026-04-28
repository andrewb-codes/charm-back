package ru.andrewb.charm.back.integration;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuthFlowIntegrationTest extends AbstractWebIntegrationTest {

    @Test
    void registrationLoginAndProfile_shouldWorkForFreshUser() throws Exception {
        String email = uniqueEmail();
        String password = "123456";

        registerUser(email, password);
        String token = loginAndGetToken(email, password);

        mockMvc.perform(
                        get("/api/v1/profile")
                                .header("Authorization", "Bearer " + token)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(email))
                .andExpect(jsonPath("$.role").value("USER"));
    }

    @Test
    void login_shouldReturn401_whenPasswordIsInvalid() throws Exception {
        String email = uniqueEmail();
        registerUser(email, "123456");

        mockMvc.perform(
                        post("/api/v1/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {"email":"%s","password":"654321"}
                                        """.formatted(email))
                )
                .andExpect(status().isUnauthorized());
    }

    @Test
    void profileEndpoint_shouldReturn401_whenNoBearerToken() throws Exception {
        mockMvc.perform(get("/api/v1/profile"))
                .andExpect(status().isUnauthorized());
    }
}
