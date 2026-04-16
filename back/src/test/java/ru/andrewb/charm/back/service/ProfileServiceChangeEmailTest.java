package ru.andrewb.charm.back.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import ru.andrewb.charm.back.dao.ProfileDao;
import ru.andrewb.charm.back.mapper.ProfileToProfileGetDtoMapper;
import ru.andrewb.charm.back.mapper.ProfileUpdateCommandToProfileMapper;
import ru.andrewb.charm.back.model.Profile;
import ru.andrewb.charm.back.model.exception.BadRequestException;
import ru.andrewb.charm.back.model.exception.DuplicateEmailException;
import ru.andrewb.charm.back.model.exception.NotFoundException;
import ru.andrewb.charm.back.service.command.EmailChangeCommand;

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
    private ProfileUpdateCommandToProfileMapper profileUpdateCommandToProfileMapper;

    private ProfileService service;
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        passwordEncoder = new BCryptPasswordEncoder();
        service = new ProfileService(
                dao,
                contentService,
                profileToProfileGetDtoMapper,
                profileUpdateCommandToProfileMapper,
                passwordEncoder
        );
    }

    @Test
    void changeEmail_shouldUpdateEmail_whenInputIsValid() {
        long profileId = 1L;

        EmailChangeCommand command = new EmailChangeCommand();
        command.setVersion(5);
        command.setNewEmail("new@mail.com");
        command.setCurrentPassword("123456");

        Profile profile = new Profile();
        profile.setId(profileId);
        profile.setVersion(1);
        profile.setEmail("old@mail.com");
        profile.setPassword(passwordEncoder.encode("123456"));

        when(dao.findById(profileId)).thenReturn(Optional.of(profile));
        when(dao.existsEmail("new@mail.com", profileId)).thenReturn(false);

        service.changeEmail(profileId, command);

        assertEquals(5, profile.getVersion());
        assertEquals("new@mail.com", profile.getEmail());
        verify(dao).findById(profileId);
        verify(dao).existsEmail("new@mail.com", profileId);
        verify(dao).update(profile);
    }

    @Test
    void changeEmail_shouldThrowNotFoundException_whenProfileDoesNotExist() {
        long profileId = 1L;

        EmailChangeCommand command = new EmailChangeCommand();
        command.setVersion(1);
        command.setNewEmail("new@mail.com");
        command.setCurrentPassword("123456");

        when(dao.findById(profileId)).thenReturn(Optional.empty());

        NotFoundException ex = assertThrows(
                NotFoundException.class,
                () -> service.changeEmail(profileId, command)
        );

        assertEquals("error.profile.not-found", ex.getMessage());
        verify(dao).findById(profileId);
        verify(dao, never()).update(any());
    }

    @Test
    void changeEmail_shouldThrowBadRequestException_whenVersionIsMissing() {
        long profileId = 1L;

        EmailChangeCommand command = new EmailChangeCommand();
        command.setVersion(null);
        command.setNewEmail("new@mail.com");
        command.setCurrentPassword("123456");

        Profile profile = new Profile();
        profile.setId(profileId);
        profile.setEmail("old@mail.com");
        profile.setPassword(passwordEncoder.encode("123456"));

        when(dao.findById(profileId)).thenReturn(Optional.of(profile));

        BadRequestException ex = assertThrows(
                BadRequestException.class,
                () -> service.changeEmail(profileId, command)
        );

        assertEquals("error.param.required", ex.getMessage());
        verify(dao).findById(profileId);
        verify(dao, never()).update(any());
    }

    @Test
    void changeEmail_shouldThrowBadRequestException_whenCurrentPasswordIsIncorrect() {
        long profileId = 1L;

        EmailChangeCommand command = new EmailChangeCommand();
        command.setVersion(1);
        command.setNewEmail("new@mail.com");
        command.setCurrentPassword("wrong-password");

        Profile existing = new Profile();
        existing.setId(profileId);
        existing.setEmail("old@mail.com");
        existing.setPassword(passwordEncoder.encode("123456"));

        when(dao.findById(profileId)).thenReturn(Optional.of(existing));

        BadRequestException ex = assertThrows(
                BadRequestException.class,
                () -> service.changeEmail(profileId, command)
        );

        assertEquals("error.password.invalid-current", ex.getMessage());
        verify(dao).findById(profileId);
        verify(dao, never()).update(any());
    }

    @Test
    void changeEmail_shouldThrowBadRequestException_whenNewEmailMatchesCurrentEmailIgnoringCase() {
        long profileId = 1L;

        EmailChangeCommand command = new EmailChangeCommand();
        command.setVersion(1);
        command.setNewEmail("OLD@mail.com");
        command.setCurrentPassword("123456");

        Profile existing = new Profile();
        existing.setId(profileId);
        existing.setEmail("old@mail.com");
        existing.setPassword(passwordEncoder.encode("123456"));

        when(dao.findById(profileId)).thenReturn(Optional.of(existing));

        BadRequestException ex = assertThrows(
                BadRequestException.class,
                () -> service.changeEmail(profileId, command)
        );

        assertEquals("error.email.same-as-current", ex.getMessage());
        verify(dao).findById(profileId);
        verify(dao, never()).update(any());
    }

    @Test
    void changeEmail_shouldThrowDuplicateEmailException_whenNewEmailIsAlreadyTaken() {
        long profileId = 1L;

        EmailChangeCommand command = new EmailChangeCommand();
        command.setVersion(1);
        command.setNewEmail("new@mail.com");
        command.setCurrentPassword("123456");

        Profile existing = new Profile();
        existing.setId(profileId);
        existing.setEmail("old@mail.com");
        existing.setPassword(passwordEncoder.encode("123456"));

        when(dao.findById(profileId)).thenReturn(Optional.of(existing));
        when(dao.existsEmail("new@mail.com", profileId)).thenReturn(true);

        DuplicateEmailException ex = assertThrows(
                DuplicateEmailException.class,
                () -> service.changeEmail(profileId, command)
        );

        assertEquals("error.email.exists", ex.getMessage());
        verify(dao).findById(profileId);
        verify(dao).existsEmail("new@mail.com", profileId);
        verify(dao, never()).update(any());
    }
}
