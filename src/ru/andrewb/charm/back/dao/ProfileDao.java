package ru.andrewb.charm.back.dao;

import ru.andrewb.charm.back.model.Gender;
import ru.andrewb.charm.back.model.Profile;
import ru.andrewb.charm.back.model.Status;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class ProfileDao {

    private static final ProfileDao INSTANCE = new ProfileDao();

    private final ConcurrentHashMap<Long, Profile> storage;
    private final AtomicLong idStorage;

    private ProfileDao() {
        this.storage = new ConcurrentHashMap<>();

        Profile profile1 = new Profile();
        profile1.setId(1L);
        profile1.setEmail("ivanov@mail.ru");
        profile1.setPassword("123");
        profile1.setName("Ivan");
        profile1.setSurname("Ivanov");
        profile1.setBirthDate(LocalDate.parse("2007-12-03"));
        profile1.setAbout("I am QA");
        profile1.setGender(Gender.MALE);
        profile1.setStatus(Status.ACTIVE);
        this.storage.put(1L, profile1);

        Profile profile2 = new Profile();
        profile2.setId(2L);
        profile2.setEmail("sidorova@mail.ru");
        profile2.setPassword("456");
        profile2.setName("Elena");
        profile2.setSurname("Sidorova");
        profile2.setBirthDate(LocalDate.parse("2004-02-03"));
        profile2.setAbout("I am Java Dev");
        profile2.setGender(Gender.FEMALE);
        profile2.setStatus(Status.ACTIVE);
        this.storage.put(2L, profile2);

        this.idStorage = new AtomicLong(3L);
    }

    public static ProfileDao getInstance() {
        return INSTANCE;
    }

    public Profile save(Profile profile) {
        profile.setId(idStorage.getAndIncrement());
        storage.put(profile.getId(), profile);
        return profile;
    }

    public Optional<Profile> findById(Long id) {
        return Optional.ofNullable(storage.get(id));
    }

    public List<Profile> findAll() {
        return new ArrayList<>(storage.values());
    }

    public void update(Profile profile) {
        Long id = profile.getId();
        if (id == null) return;
        storage.put(id, profile);
    }

    public boolean delete(Long id) {
        return (storage.remove(id) != null);
    }

    public boolean existsEmail(String email, Long excludeId) {
        if (email == null) return false;
        String probe = email.trim();
        return storage.values().stream().anyMatch(p ->
                p.getEmail() != null
                        && p.getEmail().equalsIgnoreCase(probe)
                        && (excludeId == null || !p.getId().equals(excludeId))
        );
    }
}
