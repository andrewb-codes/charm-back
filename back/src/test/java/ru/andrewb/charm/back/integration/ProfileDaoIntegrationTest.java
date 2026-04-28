package ru.andrewb.charm.back.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.andrewb.charm.back.dao.ProfileDao;
import ru.andrewb.charm.back.dao.ProfileLikeDao;
import ru.andrewb.charm.back.model.Profile;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ProfileDaoIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private ProfileDao profileDao;

    @Autowired
    private ProfileLikeDao profileLikeDao;

    @Test
    void saveFindAndDelete_shouldWorkAgainstRealPostgres() {
        String email = "it-" + UUID.randomUUID() + "@mail.com";
        Profile profile = new Profile();
        profile.setEmail(email);
        profile.setPassword("$2a$10$testHashForIntegrationOnly0000000000000000000");

        Long id = profileDao.save(profile).getId();
        assertNotNull(id);

        Profile found = profileDao.findById(id).orElseThrow();
        assertEquals(email, found.getEmail());

        boolean deleted = profileDao.delete(id);
        assertTrue(deleted);
        assertTrue(profileDao.findById(id).isEmpty());
    }

    @Test
    void findMatches_shouldReturnMutualMatchForFreshProfiles() {
        Long firstId = createProfile("first-" + UUID.randomUUID() + "@mail.com");
        Long secondId = createProfile("second-" + UUID.randomUUID() + "@mail.com");

        profileLikeDao.likeOrDislike(firstId, secondId, true);
        profileLikeDao.likeOrDislike(secondId, firstId, true);

        var matches = profileDao.findMatches(firstId, 10, 0);

        assertEquals(1, matches.size());
        assertEquals(secondId, matches.getFirst().getId());
    }

    private Long createProfile(String email) {
        Profile profile = new Profile();
        profile.setEmail(email);
        profile.setPassword("$2a$10$testHashForIntegrationOnly0000000000000000000");
        return profileDao.save(profile).getId();
    }
}
