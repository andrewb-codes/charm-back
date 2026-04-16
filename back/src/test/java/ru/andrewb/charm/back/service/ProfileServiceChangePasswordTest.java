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
import ru.andrewb.charm.back.model.exception.NotFoundException;
import ru.andrewb.charm.back.service.command.PasswordChangeCommand;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProfileServiceChangePasswordTest {

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
    void changePassword_shouldUpdatePassword_whenInputIsValid() {
        long profileId = 1L;

        PasswordChangeCommand command = new PasswordChangeCommand();
        command.setVersion(4);
        command.setCurrentPassword("oldpass");
        command.setNewPassword("newpass");
        command.setConfirmPassword("newpass");

        Profile profile = new Profile();
        profile.setId(profileId);
        profile.setVersion(1);
        profile.setEmail("user@mail.com");
        profile.setPassword(passwordEncoder.encode("oldpass"));

        when(dao.findById(profileId)).thenReturn(Optional.of(profile));

        String oldHash = profile.getPassword();

        service.changePassword(profileId, command);

        assertEquals(4, profile.getVersion());
        assertNotEquals(oldHash, profile.getPassword());
        assertTrue(passwordEncoder.matches("newpass", profile.getPassword()));
        verify(dao).findById(profileId);
        verify(dao).update(profile);
    }

    @Test
    void changePassword_shouldThrowNotFoundException_whenProfileDoesNotExist() {
        long profileId = 1L;

        PasswordChangeCommand command = new PasswordChangeCommand();
        command.setVersion(1);
        command.setCurrentPassword("oldpass");
        command.setNewPassword("newpass");
        command.setConfirmPassword("newpass");

        when(dao.findById(profileId)).thenReturn(Optional.empty());

        NotFoundException ex = assertThrows(
                NotFoundException.class,
                () -> service.changePassword(profileId, command)
        );

        assertEquals("error.profile.not-found", ex.getMessage());
        verify(dao).findById(profileId);
        verify(dao, never()).update(any());
    }

    @Test
    void changePassword_shouldThrowBadRequestException_whenVersionIsMissing() {
        long profileId = 1L;

        PasswordChangeCommand command = new PasswordChangeCommand();
        command.setVersion(null);
        command.setCurrentPassword("oldpass");
        command.setNewPassword("newpass");
        command.setConfirmPassword("newpass");

        Profile profile = new Profile();
        profile.setId(profileId);
        profile.setPassword(passwordEncoder.encode("oldpass"));

        when(dao.findById(profileId)).thenReturn(Optional.of(profile));

        BadRequestException ex = assertThrows(
                BadRequestException.class,
                () -> service.changePassword(profileId, command)
        );

        assertEquals("error.param.required", ex.getMessage());
        verify(dao).findById(profileId);
        verify(dao, never()).update(any());
    }

    @Test
    void changePassword_shouldThrowBadRequestException_whenConfirmPasswordIsBlank() {
        long profileId = 1L;

        PasswordChangeCommand command = new PasswordChangeCommand();
        command.setVersion(1);
        command.setCurrentPassword("oldpass");
        command.setNewPassword("newpass");
        command.setConfirmPassword("   ");

        Profile profile = new Profile();
        profile.setId(profileId);
        profile.setPassword(passwordEncoder.encode("oldpass"));

        when(dao.findById(profileId)).thenReturn(Optional.of(profile));

        BadRequestException ex = assertThrows(
                BadRequestException.class,
                () -> service.changePassword(profileId, command)
        );

        assertEquals("error.password.required", ex.getMessage());
        verify(dao).findById(profileId);
        verify(dao, never()).update(any());
    }

    @Test
    void changePassword_shouldThrowBadRequestException_whenConfirmPasswordDoesNotMatch() {
        long profileId = 1L;

        PasswordChangeCommand command = new PasswordChangeCommand();
        command.setVersion(1);
        command.setCurrentPassword("oldpass");
        command.setNewPassword("newpass");
        command.setConfirmPassword("otherpass");

        Profile existing = new Profile();
        existing.setId(profileId);
        existing.setPassword(passwordEncoder.encode("oldpass"));

        when(dao.findById(profileId)).thenReturn(Optional.of(existing));

        BadRequestException ex = assertThrows(
                BadRequestException.class,
                () -> service.changePassword(profileId, command)
        );

        assertEquals("error.password.mismatch", ex.getMessage());
        verify(dao).findById(profileId);
        verify(dao, never()).update(any());
    }

    @Test
    void changePassword_shouldThrowBadRequestException_whenCurrentPasswordIsIncorrect() {
        long profileId = 1L;

        PasswordChangeCommand command = new PasswordChangeCommand();
        command.setVersion(1);
        command.setCurrentPassword("wrongpass");
        command.setNewPassword("newpass");
        command.setConfirmPassword("newpass");

        Profile profile = new Profile();
        profile.setId(profileId);
        profile.setPassword(passwordEncoder.encode("oldpass"));

        when(dao.findById(profileId)).thenReturn(Optional.of(profile));

        BadRequestException ex = assertThrows(
                BadRequestException.class,
                () -> service.changePassword(profileId, command)
        );

        assertEquals("error.password.invalid-current", ex.getMessage());
        verify(dao).findById(profileId);
        verify(dao, never()).update(any());
    }

    @Test
    void changePassword_shouldThrowBadRequestException_whenNewPasswordMatchesCurrentPassword() {
        long profileId = 1L;

        PasswordChangeCommand command = new PasswordChangeCommand();
        command.setVersion(1);
        command.setCurrentPassword("samepass");
        command.setNewPassword("samepass");
        command.setConfirmPassword("samepass");

        Profile profile = new Profile();
        profile.setId(profileId);
        profile.setPassword(passwordEncoder.encode("samepass"));

        when(dao.findById(profileId)).thenReturn(Optional.of(profile));

        BadRequestException ex = assertThrows(
                BadRequestException.class,
                () -> service.changePassword(profileId, command)
        );

        assertEquals("error.password.same-as-current", ex.getMessage());
        verify(dao).findById(profileId);
        verify(dao, never()).update(any());
    }
}
