package ru.andrewb.charm.back.integration;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class ProfileRestIntegrationTest extends AbstractWebIntegrationTest {

    @Test
    void changeEmail_shouldInvalidateOldLoginAndAllowNewLogin() throws Exception {
        String oldEmail = uniqueEmail();
        String newEmail = uniqueEmail();
        String password = "123456";

        registerUser(oldEmail, password);
        String token = loginAndGetToken(oldEmail, password);
        int version = getCurrentProfileVersion(token);

        mockMvc.perform(
                        put("/api/v1/profile/email")
                                .header("Authorization", "Bearer " + token)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {"newEmail":"%s","currentPassword":"%s","version":%d}
                                        """.formatted(newEmail, password, version))
                )
                .andExpect(status().isNoContent());

        mockMvc.perform(
                        post("/api/v1/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {"email":"%s","password":"%s"}
                                        """.formatted(oldEmail, password))
                )
                .andExpect(status().isUnauthorized());

        String refreshedToken = loginAndGetToken(newEmail, password);
        mockMvc.perform(
                        get("/api/v1/profile")
                                .header("Authorization", "Bearer " + refreshedToken)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(newEmail));
    }

    @Test
    void updateProfile_shouldReturn409_whenUsingStaleVersion() throws Exception {
        String email = uniqueEmail();
        String password = "123456";

        registerUser(email, password);
        String token = loginAndGetToken(email, password);
        int version = getCurrentProfileVersion(token);

        mockMvc.perform(
                        put("/api/v1/profile")
                                .header("Authorization", "Bearer " + token)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {"name":"First","version":%d}
                                        """.formatted(version))
                )
                .andExpect(status().isNoContent());

        mockMvc.perform(
                        put("/api/v1/profile")
                                .header("Authorization", "Bearer " + token)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {"surname":"Second","version":%d}
                                        """.formatted(version))
                )
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.errors[0].code").value("error.optimistic-lock"));
    }

    private int getCurrentProfileVersion(String token) throws Exception {
        MvcResult result = mockMvc.perform(
                        get("/api/v1/profile")
                                .header("Authorization", "Bearer " + token)
                )
                .andExpect(status().isOk())
                .andReturn();

        JsonNode json = readJson(result);
        JsonNode versionNode = json.get("version");
        assertNotNull(versionNode);
        return versionNode.asInt();
    }
}
