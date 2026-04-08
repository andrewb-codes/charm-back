package ru.andrewb.charm.back.service;

import org.springframework.stereotype.Service;
import ru.andrewb.charm.back.dao.ProfileDao;
import ru.andrewb.charm.back.dto.*;
import ru.andrewb.charm.back.mapper.ProfileToProfileGetDtoMapper;
import ru.andrewb.charm.back.mapper.ProfileToUserDetailsDtoMapper;
import ru.andrewb.charm.back.mapper.ProfileUpdateDtoToProfileMapper;
import ru.andrewb.charm.back.model.Profile;
import ru.andrewb.charm.back.model.exception.BadRequestException;
import ru.andrewb.charm.back.model.exception.DuplicateEmailException;
import ru.andrewb.charm.back.model.exception.NotFoundException;
import ru.andrewb.charm.back.model.exception.StorageException;
import ru.andrewb.charm.back.security.PasswordHasher;
import ru.andrewb.charm.back.validator.EmailUtils;
import ru.andrewb.charm.back.validator.PasswordUtils;

import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

@Service
public class ProfileService {

    private final ProfileDao dao;
    private final ContentService contentService;
    private final ProfileToProfileGetDtoMapper profileToProfileGetDtoMapper;
    private final ProfileUpdateDtoToProfileMapper profileUpdateDtoToProfileMapper;
    private final ProfileToUserDetailsDtoMapper profileToUserDetailsDtoMapper;

    public ProfileService(
            ProfileDao dao,
            ContentService contentService,
            ProfileToProfileGetDtoMapper profileToProfileGetDtoMapper,
            ProfileUpdateDtoToProfileMapper profileUpdateDtoToProfileMapper,
            ProfileToUserDetailsDtoMapper profileToUserDetailsDtoMapper
    ) {
        this.dao = dao;
        this.contentService = contentService;
        this.profileToProfileGetDtoMapper = profileToProfileGetDtoMapper;
        this.profileUpdateDtoToProfileMapper = profileUpdateDtoToProfileMapper;
        this.profileToUserDetailsDtoMapper = profileToUserDetailsDtoMapper;
    }

    public Long save(RegistrationDto dto) {
        String email = EmailUtils.requireValidOrThrow(dto.getEmail());
        if (dao.existsEmail(email, null)) {
            throw new DuplicateEmailException("error.email.exists");
        }

        String pwd = PasswordUtils.requireValidOrThrow(dto.getPassword(), 6);
        String hash = PasswordHasher.hashPwd(pwd);

        Profile p = new Profile();
        p.setEmail(email);
        p.setPassword(hash);

        return dao.save(p).getId();
    }

    public Optional<ProfileGetDto> findById(Long id) {
        if (id == null) return Optional.empty();
        return dao.findById(id).map(profileToProfileGetDtoMapper::map);
    }

    public ProfileGetDto findByIdOrThrow(Long id) {
        return dao.findById(id)
                .map(profileToProfileGetDtoMapper::map)
                .orElseThrow(() -> new NotFoundException("error.profile.not-found"));
    }

    public List<ProfileGetDto> findAll(ProfileFilter filter) {
        return dao.findAll(filter).stream().map(profileToProfileGetDtoMapper::map).toList();
    }

    public List<ProfileGetDto> findMatches(Long id, int limit, int offset) {
        return dao.findMatches(id, limit, offset).stream()
                .map(profileToProfileGetDtoMapper::map)
                .toList();
    }

    public void update(Long id, ProfileUpdateDto dto) {
        var existing = dao.findById(id)
                .orElseThrow(() -> new NotFoundException("error.profile.not-found"));

        if (dto.getVersion() == null) {
            throw new BadRequestException("error.param.required");
        }

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
                throw new StorageException("error.profile.photo", e);
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

    public void changeEmail(Long id, EmailChangeDto dto) {
        var existing = dao.findById(id)
                .orElseThrow(() -> new NotFoundException("error.profile.not-found"));

        if (dto.getVersion() == null) {
            throw new BadRequestException("error.param.required");
        }
        existing.setVersion(dto.getVersion());

        String pwd = PasswordUtils.requireValidOrThrow(dto.getCurrentPassword(), 6);
        String hash = existing.getPassword();

        if (!PasswordHasher.checkPwd(pwd, hash)) {
            throw new BadRequestException("error.password.invalid-current");
        }

        String newEmail = EmailUtils.requireValidOrThrow(dto.getNewEmail());
        if (newEmail.equalsIgnoreCase(existing.getEmail())) {
            throw new BadRequestException("error.email.same-as-current");
        }
        if (dao.existsEmail(newEmail, id)) {
            throw new DuplicateEmailException("error.email.exists");
        }

        existing.setEmail(newEmail);
        dao.update(existing);
    }

    public void changePassword(Long id, PasswordChangeDto dto) {
        var existing = dao.findById(id)
                .orElseThrow(() -> new NotFoundException("error.profile.not-found"));

        if (dto.getVersion() == null) {
            throw new BadRequestException("error.param.required");
        }
        existing.setVersion(dto.getVersion());

        String currPwd = PasswordUtils.requireValidOrThrow(dto.getCurrentPassword(), 6);
        String newPwd = PasswordUtils.requireValidOrThrow(dto.getNewPassword(), 6);
        String confirmPwd = PasswordUtils.normalize(dto.getConfirmPassword());

        if (!PasswordUtils.hasText(confirmPwd)) {
            throw new BadRequestException("error.password.required");
        }
        if (!confirmPwd.equals(newPwd)) {
            throw new BadRequestException("error.password.mismatch");
        }

        String oldHash = existing.getPassword();
        if (!PasswordHasher.checkPwd(currPwd, oldHash)) {
            throw new BadRequestException("error.password.invalid-current");
        }
        if (PasswordHasher.checkPwd(newPwd, oldHash)) {
            throw new BadRequestException("error.password.same-as-current");
        }

        String newHash = PasswordHasher.hashPwd(newPwd);
        existing.setPassword(newHash);
        dao.update(existing);
    }

    public UserDetailsDto login(LoginDto dto) {
        String email = EmailUtils.requireValidOrThrow(dto.getEmail());
        var existing = dao.findByEmail(email)
                .orElseThrow(() -> new BadRequestException("error.login.bad-credentials"));

        String pwd = PasswordUtils.normalize(dto.getPassword());
        if (!PasswordHasher.checkPwd(pwd, existing.getPassword())) {
            throw new BadRequestException("error.login.bad-credentials");
        }

        return profileToUserDetailsDtoMapper.map(existing);
    }
}
