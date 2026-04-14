package ru.andrewb.charm.back.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import ru.andrewb.charm.back.dao.ProfileDao;
import ru.andrewb.charm.back.dto.EmailChangeDto;
import ru.andrewb.charm.back.mapper.ProfileToProfileGetDtoMapper;
import ru.andrewb.charm.back.mapper.ProfileUpdateDtoToProfileMapper;
import ru.andrewb.charm.back.model.Profile;
import ru.andrewb.charm.back.model.exception.BadRequestException;
import ru.andrewb.charm.back.model.exception.DuplicateEmailException;
import ru.andrewb.charm.back.model.exception.NotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProfileServiceChangeEmailTest {

    @Mock
    private ProfileDao dao;

    @Mock
    private ContentService contentService;

    @Mock
    private ProfileToProfileGetDtoMapper profileToProfileGetDtoMapper;

    @Mock
    private ProfileUpdateDtoToProfileMapper profileUpdateDtoToProfileMapper;

    private ProfileService service;
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        passwordEncoder = new BCryptPasswordEncoder();
        service = new ProfileService(
                dao,
                contentService,
                profileToProfileGetDtoMapper,
                profileUpdateDtoToProfileMapper,
                passwordEncoder
        );
    }

    @Test
    void changeEmail_shouldUpdateEmail_whenInputIsValid() {
        long profileId = 1L;

        EmailChangeDto dto = new EmailChangeDto();
        dto.setVersion(5);
        dto.setNewEmail("new@mail.com");
        dto.setCurrentPassword("123456");

        Profile profile = new Profile();
        profile.setId(profileId);
        profile.setVersion(1);
        profile.setEmail("old@mail.com");
        profile.setPassword(passwordEncoder.encode("123456"));

        when(dao.findById(profileId)).thenReturn(Optional.of(profile));
        when(dao.existsEmail("new@mail.com", profileId)).thenReturn(false);

        service.changeEmail(profileId, dto);

        assertEquals(5, profile.getVersion());
        assertEquals("new@mail.com", profile.getEmail());
        verify(dao).findById(profileId);
        verify(dao).existsEmail("new@mail.com", profileId);
        verify(dao).update(profile);
    }

    @Test
    void changeEmail_shouldThrowNotFoundException_whenProfileDoesNotExist() {
        long profileId = 1L;

        EmailChangeDto dto = new EmailChangeDto();
        dto.setVersion(1);
        dto.setNewEmail("new@mail.com");
        dto.setCurrentPassword("123456");

        when(dao.findById(profileId)).thenReturn(Optional.empty());

        NotFoundException ex = assertThrows(
                NotFoundException.class,
                () -> service.changeEmail(profileId, dto)
        );

        assertEquals("error.profile.not-found", ex.getMessage());
        verify(dao).findById(profileId);
        verify(dao, never()).update(any());
    }

    @Test
    void changeEmail_shouldThrowBadRequestException_whenVersionIsMissing() {
        long profileId = 1L;

        EmailChangeDto dto = new EmailChangeDto();
        dto.setVersion(null);
        dto.setNewEmail("new@mail.com");
        dto.setCurrentPassword("123456");

        Profile profile = new Profile();
        profile.setId(profileId);
        profile.setEmail("old@mail.com");
        profile.setPassword(passwordEncoder.encode("123456"));

        when(dao.findById(profileId)).thenReturn(Optional.of(profile));

        BadRequestException ex = assertThrows(
                BadRequestException.class,
                () -> service.changeEmail(profileId, dto)
        );

        assertEquals("error.param.required", ex.getMessage());
        verify(dao).findById(profileId);
        verify(dao, never()).update(any());
    }

    @Test
    void changeEmail_shouldThrowBadRequestException_whenCurrentPasswordIsIncorrect() {
        long profileId = 1L;

        EmailChangeDto dto = new EmailChangeDto();
        dto.setVersion(1);
        dto.setNewEmail("new@mail.com");
        dto.setCurrentPassword("wrong-password");

        Profile existing = new Profile();
        existing.setId(profileId);
        existing.setEmail("old@mail.com");
        existing.setPassword(passwordEncoder.encode("123456"));

        when(dao.findById(profileId)).thenReturn(Optional.of(existing));

        BadRequestException ex = assertThrows(
                BadRequestException.class,
                () -> service.changeEmail(profileId, dto)
        );

        assertEquals("error.password.invalid-current", ex.getMessage());
        verify(dao).findById(profileId);
        verify(dao, never()).update(any());
    }

    @Test
    void changeEmail_shouldThrowBadRequestException_whenNewEmailMatchesCurrentEmailIgnoringCase() {
        long profileId = 1L;

        EmailChangeDto dto = new EmailChangeDto();
        dto.setVersion(1);
        dto.setNewEmail("OLD@mail.com");
        dto.setCurrentPassword("123456");

        Profile existing = new Profile();
        existing.setId(profileId);
        existing.setEmail("old@mail.com");
        existing.setPassword(passwordEncoder.encode("123456"));

        when(dao.findById(profileId)).thenReturn(Optional.of(existing));

        BadRequestException ex = assertThrows(
                BadRequestException.class,
                () -> service.changeEmail(profileId, dto)
        );

        assertEquals("error.email.same-as-current", ex.getMessage());
        verify(dao).findById(profileId);
        verify(dao, never()).update(any());
    }

    @Test
    void changeEmail_shouldThrowDuplicateEmailException_whenNewEmailIsAlreadyTaken() {
        long profileId = 1L;

        EmailChangeDto dto = new EmailChangeDto();
        dto.setVersion(1);
        dto.setNewEmail("new@mail.com");
        dto.setCurrentPassword("123456");

        Profile existing = new Profile();
        existing.setId(profileId);
        existing.setEmail("old@mail.com");
        existing.setPassword(passwordEncoder.encode("123456"));

        when(dao.findById(profileId)).thenReturn(Optional.of(existing));
        when(dao.existsEmail("new@mail.com", profileId)).thenReturn(true);

        DuplicateEmailException ex = assertThrows(
                DuplicateEmailException.class,
                () -> service.changeEmail(profileId, dto)
        );

        assertEquals("error.email.exists", ex.getMessage());
        verify(dao).findById(profileId);
        verify(dao).existsEmail("new@mail.com", profileId);
        verify(dao, never()).update(any());
    }
}
