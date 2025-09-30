package ru.andrewb.charm.back.service;

import ru.andrewb.charm.back.dao.ProfileDao;
import ru.andrewb.charm.back.dto.ProfileGetDto;
import ru.andrewb.charm.back.dto.ProfileUpdateDto;
import ru.andrewb.charm.back.dto.RegistrationDto;
import ru.andrewb.charm.back.mapper.ProfileToProfileGetDtoMapper;
import ru.andrewb.charm.back.mapper.ProfileUpdateDtoToProfileMapper;
import ru.andrewb.charm.back.mapper.RegistrationDtoToProfileMapper;
import ru.andrewb.charm.back.model.Profile;
import ru.andrewb.charm.back.model.exception.BadRequestException;
import ru.andrewb.charm.back.model.exception.DuplicateEmailException;
import ru.andrewb.charm.back.model.exception.NotFoundException;

import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

public class ProfileService {

    private static final ProfileService INSTANCE = new ProfileService();

    private final ProfileDao dao = ProfileDao.getInstance();

    private final ProfileToProfileGetDtoMapper profileToProfileGetDtoMapper = ProfileToProfileGetDtoMapper.getInstance();

    private final RegistrationDtoToProfileMapper registrationDtoToProfileMapper = RegistrationDtoToProfileMapper.getInstance();

    private final ProfileUpdateDtoToProfileMapper profileUpdateDtoToProfileMapper = ProfileUpdateDtoToProfileMapper.getInstance();

    private ProfileService() {
    }

    public static ProfileService getInstance() {
        return INSTANCE;
    }

    public Long save(RegistrationDto dto) {
        String email = normalizeEmail(dto.getEmail());
        requireValidEmail(email);
        if (dao.existsEmail(email, null)) {
            throw new DuplicateEmailException("email already exists");
        }
        Profile p = registrationDtoToProfileMapper.map(dto);
        return dao.save(p).getId();
    }

    public Optional<ProfileGetDto> findById(Long id) {
        if (id == null) return Optional.empty();
        return dao.findById(id).map(profileToProfileGetDtoMapper::map);
    }

    public List<ProfileGetDto> findAll() {
        return dao.findAll().stream().map(profileToProfileGetDtoMapper::map).toList();
    }

    public void update(Long id, ProfileUpdateDto dto) {
        var existing = dao.findById(id)
                .orElseThrow(() -> new NotFoundException("profile not found"));

        if (dto.getEmail() != null) {
            String email = normalizeEmail(dto.getEmail());
            requireValidEmail(email);
            if (dao.existsEmail(email, id)) throw new DuplicateEmailException("email already exists");
            dto.setEmail(email);
        }

        Profile profile = profileUpdateDtoToProfileMapper.map(dto, existing);
        profile.setId(id);

        // TODO: БИЗНЕС-ПРАВИЛО (при переходе в ACTIVE требуем заполненность обязательных полей)
        dao.update(profile);
    }

    public boolean delete(Long id) {
        if (id == null) return false;
        return dao.delete(id);
    }

    private static final Pattern EMAIL_RE = Pattern.compile(
            "^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,}$",
            Pattern.CASE_INSENSITIVE
    );

    private static String normalizeEmail(String email) {
        return email == null ? null : email.trim();
    }

    private void requireValidEmail(String email) {
        String e = normalizeEmail(email);
        if (e == null || e.isBlank())
            throw new BadRequestException("email is required");
        if (!EMAIL_RE.matcher(e).matches())
            throw new BadRequestException("invalid email");
    }
}
