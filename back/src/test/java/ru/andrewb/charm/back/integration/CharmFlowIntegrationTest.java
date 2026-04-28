package ru.andrewb.charm.back.integration;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MvcResult;
import ru.andrewb.charm.back.dao.ProfileDao;
import ru.andrewb.charm.back.dao.ProfileLikeDao;
import ru.andrewb.charm.back.model.Profile;
import ru.andrewb.charm.back.model.Status;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class CharmFlowIntegrationTest extends AbstractWebIntegrationTest {

    @Autowired
    private ProfileDao profileDao;

    @Autowired
    private ProfileLikeDao profileLikeDao;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void charmFlow_shouldReturnNextCandidatePersistLikeAndAdvanceQueue() throws Exception {
        String email = uniqueEmail();
        String password = "123456";

        Long currentUserId = registerUser(email, password);
        String token = loginAndGetToken(email, password);

        markExistingActiveProfilesAsSeen(currentUserId);

        Long candidateA = createActiveCandidate("Alice");
        Long candidateB = createActiveCandidate("Bella");
        Set<Long> expectedIds = Set.of(candidateA, candidateB);

        Long firstCandidateId = extractProfileId(
                mockMvc.perform(
                                get("/api/v1/charm")
                                        .header("Authorization", "Bearer " + token)
                        )
                        .andExpect(status().isOk())
                        .andReturn()
        );

        assertTrue(expectedIds.contains(firstCandidateId));

        Long secondCandidateId = extractProfileId(
                mockMvc.perform(
                                post("/api/v1/charm")
                                        .header("Authorization", "Bearer " + token)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content("""
                                                {"toProfileId":%d,"action":"LIKE"}
                                                """.formatted(firstCandidateId))
                        )
                        .andExpect(status().isOk())
                        .andReturn()
        );

        assertTrue(expectedIds.contains(secondCandidateId));
        assertNotEquals(firstCandidateId, secondCandidateId);

        Integer likes = jdbcTemplate.queryForObject(
                """
                SELECT COUNT(*)
                FROM profile_like
                WHERE (a_profile = LEAST(?, ?) AND b_profile = GREATEST(?, ?))
                  AND ((a_profile = ? AND liked_a IS TRUE) OR (b_profile = ? AND liked_b IS TRUE))
                """,
                Integer.class,
                firstCandidateId,
                currentUserId,
                firstCandidateId,
                currentUserId,
                currentUserId,
                currentUserId
        );

        assertNotNull(likes);
        assertTrue(likes > 0);
    }

    private void markExistingActiveProfilesAsSeen(Long currentUserId) {
        List<Long> existingActiveProfileIds = jdbcTemplate.queryForList(
                """
                SELECT id
                FROM profile
                WHERE status = 'ACTIVE'
                  AND id <> ?
                """,
                Long.class,
                currentUserId
        );

        existingActiveProfileIds.forEach(id -> profileLikeDao.likeOrDislike(currentUserId, id, false));
    }

    private Long extractProfileId(MvcResult result) throws Exception {
        JsonNode json = readJson(result);
        JsonNode profileNode = json.get("profile");
        assertNotNull(profileNode);
        JsonNode idNode = profileNode.get("id");
        assertNotNull(idNode);
        return idNode.asLong();
    }

    private Long createActiveCandidate(String name) {
        Profile profile = new Profile();
        profile.setEmail(uniqueEmail());
        profile.setPassword("$2a$10$testHashForIntegrationOnly0000000000000000000");

        Long id = profileDao.save(profile).getId();
        Profile stored = profileDao.findById(id).orElseThrow();
        stored.setName(name);
        stored.setStatus(Status.ACTIVE);
        profileDao.update(stored);
        return id;
    }
}
