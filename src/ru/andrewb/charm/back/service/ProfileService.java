package ru.andrewb.charm.back.service;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
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
import ru.andrewb.charm.back.model.exception.StorageException;
import ru.andrewb.charm.back.normalizer.ProfileFilterDefaults;
import ru.andrewb.charm.back.utils.EmailUtils;
import ru.andrewb.charm.back.utils.PasswordUtils;

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
        String email = EmailUtils.requireValidOrThrow(dto.getEmail());
        if (dao.existsEmail(email, null)) {
            throw new DuplicateEmailException("error.email.exists");
        }

        String pwd = PasswordUtils.requireValidOrThrow(dto.getPassword(), 6);

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
                .orElseThrow(() -> new NotFoundException("error.profile.not-found"));
    }

    public List<ProfileGetDto> findAll(ProfileFilter filter) {
        ProfileFilterDefaults.normalize(filter);
        return dao.findAll(filter).stream().map(profileToProfileGetDtoMapper::map).toList();
    }

    public List<ProfileGetDto> findMatches(Long id, int limit, int offset) {
        return dao.findMatches(id, limit, offset).stream()
                .map(profileToProfileGetDtoMapper::map)
                .toList();
    }

    public void update(long id, ProfileUpdateDto dto) {
        var existing = dao.findById(id)
                .orElseThrow(() -> new NotFoundException("error.profile.not-found"));

        Profile p = profileUpdateDtoToProfileMapper.map(dto, existing);

        var part = dto.getPhoto();
        var old = existing.getPhoto();
        if (part != null && part.getSize() > 0) {
            try {
                if (old != null && !old.isBlank()) {
                    contentService.delete("profile", String.valueOf(id), old);
                }
                String fileName = Paths.get(part.getSubmittedFileName()).getFileName().toString();
                contentService.upload(part.getInputStream(), "profile", String.valueOf(id), fileName);
                p.setPhoto(fileName);
            } catch (Exception e) {
                throw new StorageException("error.service.photo", e);
            }
        }

        // TODO: БИЗНЕС-ПРАВИЛО (при переходе в ACTIVE требуем заполненность обязательных полей)
        dao.update(p);
    }

    public void updateStatuses(List<ProfileUpdateStatusDto> dtoList) {
        if (dtoList.isEmpty()) return;
        dao.updateStatuses(dtoList);
    }

    public boolean delete(Long id) {
        if (id == null) return false;
        contentService.deleteTree("profile", String.valueOf(id));
        return dao.delete(id);
    }

    public void changeEmail(long id, EmailChangeDto dto) {
        var p = dao.findById(id)
                .orElseThrow(() -> new NotFoundException("error.profile.not-found"));

        String currPwd = PasswordUtils.normalize(dto.getCurrentPassword());
        if (!PasswordUtils.hasText(currPwd) || !p.getPassword().equals(currPwd)) {
            throw new BadRequestException("error.password.invalid-current");
        }

        String newEmail = EmailUtils.requireValidOrThrow(dto.getNewEmail());
        if (newEmail.equalsIgnoreCase(p.getEmail())) {
            throw new BadRequestException("error.email.same-as-current");
        }
        if (dao.existsEmail(newEmail, id)) {
            throw new DuplicateEmailException("error.email.exists");
        }

        p.setEmail(newEmail);
        dao.update(p);
    }

    public void changePassword(long id, PasswordChangeDto dto) {
        var p = dao.findById(id)
                .orElseThrow(() -> new NotFoundException("error.profile.not-found"));

        String currPwd = PasswordUtils.normalize(dto.getCurrentPassword());
        if (!PasswordUtils.hasText(currPwd) || !p.getPassword().equals(currPwd)) {
            throw new BadRequestException("error.password.invalid-current");
        }

        String newPwd = PasswordUtils.requireValidOrThrow(dto.getNewPassword(), 6);
        if (newPwd.equals(currPwd)) {
            throw new BadRequestException("error.password.same-as-current");
        }

        p.setPassword(newPwd);
        dao.update(p);
    }

    public UserDetailsDto login(LoginDto dto) {
        String email = EmailUtils.requireValidOrThrow(dto.getEmail());
        var existing = dao.findByEmail(email)
                .orElseThrow(() -> new BadRequestException("error.login.bad-credentials"));

        String pwd = PasswordUtils.normalize(dto.getPassword());
        if (!existing.getPassword().equals(pwd)) {
            throw new BadRequestException("error.login.bad-credentials");
        }

        return profileToUserDetailsDtoMapper.map(existing);
    }
}
