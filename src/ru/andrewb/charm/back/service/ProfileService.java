package ru.andrewb.charm.back.service;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import ru.andrewb.charm.back.dao.ProfileDao;
import ru.andrewb.charm.back.dto.*;
import ru.andrewb.charm.back.mapper.ProfileToProfileGetDtoMapper;
import ru.andrewb.charm.back.mapper.ProfileToUserDetailsDtoMapper;
import ru.andrewb.charm.back.mapper.ProfileUpdateDtoToProfileMapper;
import ru.andrewb.charm.back.mapper.RegistrationDtoToProfileMapper;
import ru.andrewb.charm.back.model.Profile;
import ru.andrewb.charm.back.model.exception.BadRequestException;
import ru.andrewb.charm.back.model.exception.DuplicateEmailException;
import ru.andrewb.charm.back.model.exception.NotFoundException;
import ru.andrewb.charm.back.utils.Emails;
import ru.andrewb.charm.back.utils.Passwords;

import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ProfileService {

    private static final ProfileService INSTANCE = new ProfileService();

    private final ProfileDao dao = ProfileDao.getInstance();

    private final ContentService contentService = ContentService.getInstance();

    private final ProfileToProfileGetDtoMapper profileToProfileGetDtoMapper = ProfileToProfileGetDtoMapper.getInstance();

    private final RegistrationDtoToProfileMapper registrationDtoToProfileMapper = RegistrationDtoToProfileMapper.getInstance();

    private final ProfileUpdateDtoToProfileMapper profileUpdateDtoToProfileMapper = ProfileUpdateDtoToProfileMapper.getInstance();

    private final ProfileToUserDetailsDtoMapper profileToUserDetailsDtoMapper = ProfileToUserDetailsDtoMapper.getInstance();

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

    @SneakyThrows
    public void update(long id, ProfileUpdateDto dto) {
        var existing = dao.findById(id)
                .orElseThrow(() -> new NotFoundException("profile not found"));

        Profile p = profileUpdateDtoToProfileMapper.map(dto, existing);

        var part = dto.getPhoto();
        var old = existing.getPhoto();
        if (part != null && part.getSize() > 0) {
            if (old != null && !old.isBlank()) {
                contentService.delete("profile", String.valueOf(id), old);
            }
            String fileName = Paths.get(part.getSubmittedFileName()).getFileName().toString();
            contentService.upload(part.getInputStream(), "profile", String.valueOf(id), fileName);
            p.setPhoto(fileName);
        }

        // TODO: БИЗНЕС-ПРАВИЛО (при переходе в ACTIVE требуем заполненность обязательных полей)
        dao.update(p);
    }

    public boolean delete(Long id) {
        if (id == null) return false;
        contentService.deleteTree("profile", String.valueOf(id));
        return dao.delete(id);
    }

    public void changeEmail(long id, EmailChangeDto dto) {
        var p = dao.findById(id)
                .orElseThrow(() -> new NotFoundException("profile not found"));

        String currPwd = Passwords.normalize(dto.getCurrentPassword());
        if (!Passwords.hasText(currPwd) || !p.getPassword().equals(currPwd)) {
            throw new BadRequestException("invalid password");
        }

        String newEmail = Emails.requireValidOrThrow(dto.getNewEmail());
        if (newEmail.equalsIgnoreCase(p.getEmail())) {
            throw new BadRequestException("same as current email");
        }
        if (dao.existsEmail(newEmail, id)) {
            throw new DuplicateEmailException("email already exists");
        }

        p.setEmail(newEmail);
        dao.update(p);
    }

    public void changePassword(long id, PasswordChangeDto dto) {
        var p = dao.findById(id)
                .orElseThrow(() -> new NotFoundException("profile not found"));

        String currPwd = Passwords.normalize(dto.getCurrentPassword());
        if (!Passwords.hasText(currPwd) || !p.getPassword().equals(currPwd)) {
            throw new BadRequestException("invalid password");
        }

        String newPwd = Passwords.requireValidOrThrow(dto.getNewPassword(), 6);
        if (newPwd.equals(currPwd)) {
            throw new BadRequestException("same as current password");
        }

        p.setPassword(newPwd);
        dao.update(p);
    }

    public UserDetailsDto login(LoginDto dto) {
        String email = Emails.requireValidOrThrow(dto.getEmail());
        var existing = dao.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("profile not found"));

        String pwd = Passwords.normalize(dto.getPassword());
        if (!existing.getPassword().equals(pwd)) {
            throw new BadRequestException("invalid password");
        }

        return profileToUserDetailsDtoMapper.map(existing);
    }
}
