package ru.andrewb.charm.back.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.andrewb.charm.back.dao.ProfileDao;
import ru.andrewb.charm.back.dto.ProfileGetDto;
import ru.andrewb.charm.back.dto.ProfilesFilter;
import ru.andrewb.charm.back.mapper.ProfileToProfileGetDtoMapper;
import ru.andrewb.charm.back.mapper.ProfileUpdateCommandToProfileMapper;
import ru.andrewb.charm.back.model.Profile;
import ru.andrewb.charm.back.model.exception.BadRequestException;
import ru.andrewb.charm.back.model.exception.DuplicateEmailException;
import ru.andrewb.charm.back.model.exception.NotFoundException;
import ru.andrewb.charm.back.model.exception.StorageException;
import ru.andrewb.charm.back.service.command.*;
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
    private final ProfileUpdateCommandToProfileMapper profileUpdateCommandToProfileMapper;
    private final PasswordEncoder passwordEncoder;

    public ProfileService(
            ProfileDao dao,
            ContentService contentService,
            ProfileToProfileGetDtoMapper profileToProfileGetDtoMapper,
            ProfileUpdateCommandToProfileMapper profileUpdateCommandToProfileMapper,
            PasswordEncoder passwordEncoder
    ) {
        this.dao = dao;
        this.contentService = contentService;
        this.profileToProfileGetDtoMapper = profileToProfileGetDtoMapper;
        this.profileUpdateCommandToProfileMapper = profileUpdateCommandToProfileMapper;
        this.passwordEncoder = passwordEncoder;
    }

    public Long save(RegistrationCommand command) {
        String email = EmailUtils.requireValidOrThrow(command.getEmail());
        if (dao.existsEmail(email, null)) {
            throw new DuplicateEmailException("error.email.exists");
        }

        String pwd = PasswordUtils.requireValidOrThrow(command.getPassword(), 6);
        String hash = passwordEncoder.encode(pwd);

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

    public List<ProfileGetDto> findAll(ProfilesFilter filter) {
        return dao.findAll(filter).stream().map(profileToProfileGetDtoMapper::map).toList();
    }

    public List<ProfileGetDto> findMatches(Long id, int limit, int offset) {
        return dao.findMatches(id, limit, offset).stream()
                .map(profileToProfileGetDtoMapper::map)
                .toList();
    }

    public void update(Long id, ProfileUpdateCommand command, MultipartFile photo) {
        var existing = dao.findById(id)
                .orElseThrow(() -> new NotFoundException("error.profile.not-found"));

        if (command.getVersion() == null) {
            throw new BadRequestException("error.param.required");
        }

        Profile p = profileUpdateCommandToProfileMapper.map(command, existing);

        if (photo != null && !photo.isEmpty()) {
            try {
                String old = existing.getPhoto();
                if (old != null && !old.isBlank()) {
                    contentService.delete("profile", String.valueOf(id), old);
                }

                String fileName = Paths.get(photo.getOriginalFilename()).getFileName().toString();
                contentService.upload(photo.getInputStream(), "profile", String.valueOf(id), fileName);
                p.setPhoto(fileName);
            } catch (Exception e) {
                throw new StorageException("error.profile.photo", e);
            }
        }

        // TODO: БИЗНЕС-ПРАВИЛО (при переходе в ACTIVE требуем заполненность обязательных полей)
        dao.update(p);
    }

    public void updateStatuses(List<ProfileUpdateStatusCommand> commandsList) {
        if (commandsList.isEmpty()) return;
        dao.updateStatuses(commandsList);
    }

    public boolean delete(Long id) {
        if (id == null) return false;
        contentService.deleteTree("profile", String.valueOf(id));
        boolean deleted = dao.delete(id);
        if (!deleted) throw new NotFoundException("error.profile.not-found");
        return true;
    }

    public void changeEmail(Long id, EmailChangeCommand command) {
        var existing = dao.findById(id)
                .orElseThrow(() -> new NotFoundException("error.profile.not-found"));

        if (command.getVersion() == null) {
            throw new BadRequestException("error.param.required");
        }
        existing.setVersion(command.getVersion());

        String pwd = PasswordUtils.requireValidOrThrow(command.getCurrentPassword(), 6);
        String hash = existing.getPassword();

        if (!passwordEncoder.matches(pwd, hash)) {
            throw new BadRequestException("error.password.invalid-current");
        }

        String newEmail = EmailUtils.requireValidOrThrow(command.getNewEmail());
        if (newEmail.equalsIgnoreCase(existing.getEmail())) {
            throw new BadRequestException("error.email.same-as-current");
        }
        if (dao.existsEmail(newEmail, id)) {
            throw new DuplicateEmailException("error.email.exists");
        }

        existing.setEmail(newEmail);
        dao.update(existing);
    }

    public void changePassword(Long id, PasswordChangeCommand command) {
        var existing = dao.findById(id)
                .orElseThrow(() -> new NotFoundException("error.profile.not-found"));

        if (command.getVersion() == null) {
            throw new BadRequestException("error.param.required");
        }
        existing.setVersion(command.getVersion());

        String currPwd = PasswordUtils.requireValidOrThrow(command.getCurrentPassword(), 6);
        String newPwd = PasswordUtils.requireValidOrThrow(command.getNewPassword(), 6);
        String confirmPwd = PasswordUtils.normalize(command.getConfirmPassword());

        if (!PasswordUtils.hasText(confirmPwd)) {
            throw new BadRequestException("error.password.required");
        }
        if (!confirmPwd.equals(newPwd)) {
            throw new BadRequestException("error.password.mismatch");
        }

        String oldHash = existing.getPassword();
        if (!passwordEncoder.matches(currPwd, oldHash)) {
            throw new BadRequestException("error.password.invalid-current");
        }
        if (passwordEncoder.matches(newPwd, oldHash)) {
            throw new BadRequestException("error.password.same-as-current");
        }

        String newHash = passwordEncoder.encode(newPwd);
        existing.setPassword(newHash);
        dao.update(existing);
    }
}
