package ru.andrewb.charm.back.service;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.andrewb.charm.back.dao.ProfileDao;
import ru.andrewb.charm.back.dto.ProfileGetDto;
import ru.andrewb.charm.back.dto.ProfileUpdateDto;
import ru.andrewb.charm.back.dto.RegistrationDto;
import ru.andrewb.charm.back.mapper.ProfileToProfileGetDtoMapper;
import ru.andrewb.charm.back.mapper.ProfileUpdateDtoToProfileMapper;
import ru.andrewb.charm.back.mapper.RegistrationDtoToProfileMapper;
import ru.andrewb.charm.back.model.Profile;
import ru.andrewb.charm.back.model.exception.DuplicateEmailException;
import ru.andrewb.charm.back.model.exception.NotFoundException;
import ru.andrewb.charm.back.utils.Emails;
import ru.andrewb.charm.back.utils.Passwords;

import java.util.List;
import java.util.Optional;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ProfileService {

    private static final ProfileService INSTANCE = new ProfileService();

    private final ProfileDao dao = ProfileDao.getInstance();

    private final ProfileToProfileGetDtoMapper profileToProfileGetDtoMapper = ProfileToProfileGetDtoMapper.getInstance();

    private final RegistrationDtoToProfileMapper registrationDtoToProfileMapper = RegistrationDtoToProfileMapper.getInstance();

    private final ProfileUpdateDtoToProfileMapper profileUpdateDtoToProfileMapper = ProfileUpdateDtoToProfileMapper.getInstance();

    public static ProfileService getInstance() {
        return INSTANCE;
    }

    public Long save(RegistrationDto dto) {
        String email = Emails.requireValidOrThrow(dto.getEmail());
        if (dao.existsEmail(email, null)) {
            throw new DuplicateEmailException("email already exists");
        }

        String pwd = Passwords.requireValidOrThrow(dto.getPassword(), 6);

        Profile p = registrationDtoToProfileMapper.map(dto);
        p.setEmail(email);
        p.setPassword(pwd);
        return dao.save(p).getId();
    }

    public Optional<ProfileGetDto> findById(Long id) {
        if (id == null) return Optional.empty();
        return dao.findById(id).map(profileToProfileGetDtoMapper::map);
    }

    public ProfileGetDto findByIdOrThrow(long id) {
        return dao.findById(id)
                .map(profileToProfileGetDtoMapper::map)
                .orElseThrow(() -> new NotFoundException("profile not found"));
    }

    public List<ProfileGetDto> findAll() {
        return dao.findAll().stream().map(profileToProfileGetDtoMapper::map).toList();
    }

    public void update(long id, ProfileUpdateDto dto) {
        var existing = dao.findById(id)
                .orElseThrow(() -> new NotFoundException("profile not found"));

        String newEmailNormalized = null;
        if (dto.getEmail() != null) {
            newEmailNormalized = Emails.requireValidOrThrow(dto.getEmail());

            if (!newEmailNormalized.equalsIgnoreCase(existing.getEmail())
                    && dao.existsEmail(newEmailNormalized, id)) {
                throw new DuplicateEmailException("email already exists");
            }
        }

        Profile p = profileUpdateDtoToProfileMapper.map(dto, existing);
        p.setId(id);
        if (newEmailNormalized != null) {
            p.setEmail(newEmailNormalized);
        }

        // TODO: БИЗНЕС-ПРАВИЛО (при переходе в ACTIVE требуем заполненность обязательных полей)
        dao.update(p);
    }

    public boolean delete(Long id) {
        if (id == null) return false;
        return dao.delete(id);
    }
}
