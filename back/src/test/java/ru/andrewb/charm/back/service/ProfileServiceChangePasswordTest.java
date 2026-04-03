package ru.andrewb.charm.back.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.andrewb.charm.back.dao.ProfileDao;
import ru.andrewb.charm.back.dto.PasswordChangeDto;
import ru.andrewb.charm.back.mapper.ProfileToProfileGetDtoMapper;
import ru.andrewb.charm.back.mapper.ProfileToUserDetailsDtoMapper;
import ru.andrewb.charm.back.mapper.ProfileUpdateDtoToProfileMapper;
import ru.andrewb.charm.back.model.Profile;
import ru.andrewb.charm.back.model.exception.BadRequestException;
import ru.andrewb.charm.back.model.exception.NotFoundException;
import ru.andrewb.charm.back.security.PasswordHasher;

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
    private ProfileUpdateDtoToProfileMapper profileUpdateDtoToProfileMapper;

    @Mock
    private ProfileToUserDetailsDtoMapper profileToUserDetailsDtoMapper;

    private ProfileService service;

    @BeforeEach
    void setUp() {
        service = new ProfileService(
                dao,
                contentService,
                profileToProfileGetDtoMapper,
                profileUpdateDtoToProfileMapper,
                profileToUserDetailsDtoMapper
        );
    }

    @Test
    void changePassword_shouldUpdatePassword_whenInputIsValid() {
        long profileId = 1L;

        PasswordChangeDto dto = new PasswordChangeDto();
        dto.setVersion(4);
        dto.setCurrentPassword("oldpass");
        dto.setNewPassword("newpass");
        dto.setConfirmPassword("newpass");

        Profile profile = new Profile();
        profile.setId(profileId);
        profile.setVersion(1);
        profile.setEmail("user@mail.com");
        profile.setPassword(PasswordHasher.hashPwd("oldpass"));

        when(dao.findById(profileId)).thenReturn(Optional.of(profile));

        String oldHash = profile.getPassword();

        service.changePassword(profileId, dto);

        assertEquals(4, profile.getVersion());
        assertNotEquals(oldHash, profile.getPassword());
        assertTrue(PasswordHasher.checkPwd("newpass", profile.getPassword()));
        verify(dao).findById(profileId);
        verify(dao).update(profile);
    }

    @Test
    void changePassword_shouldThrowNotFoundException_whenProfileDoesNotExist() {
        long profileId = 1L;

        PasswordChangeDto dto = new PasswordChangeDto();
        dto.setVersion(1);
        dto.setCurrentPassword("oldpass");
        dto.setNewPassword("newpass");
        dto.setConfirmPassword("newpass");

        when(dao.findById(profileId)).thenReturn(Optional.empty());

        NotFoundException ex = assertThrows(
                NotFoundException.class,
                () -> service.changePassword(profileId, dto)
        );

        assertEquals("error.profile.not-found", ex.getMessage());
        verify(dao).findById(profileId);
        verify(dao, never()).update(any());
    }

    @Test
    void changePassword_shouldThrowBadRequestException_whenVersionIsMissing() {
        long profileId = 1L;

        PasswordChangeDto dto = new PasswordChangeDto();
        dto.setVersion(null);
        dto.setCurrentPassword("oldpass");
        dto.setNewPassword("newpass");
        dto.setConfirmPassword("newpass");

        Profile profile = new Profile();
        profile.setId(profileId);
        profile.setPassword(PasswordHasher.hashPwd("oldpass"));

        when(dao.findById(profileId)).thenReturn(Optional.of(profile));

        BadRequestException ex = assertThrows(
                BadRequestException.class,
                () -> service.changePassword(profileId, dto)
        );

        assertEquals("error.param.required", ex.getMessage());
        verify(dao).findById(profileId);
        verify(dao, never()).update(any());
    }

    @Test
    void changePassword_shouldThrowBadRequestException_whenConfirmPasswordIsBlank() {
        long profileId = 1L;

        PasswordChangeDto dto = new PasswordChangeDto();
        dto.setVersion(1);
        dto.setCurrentPassword("oldpass");
        dto.setNewPassword("newpass");
        dto.setConfirmPassword("   ");

        Profile profile = new Profile();
        profile.setId(profileId);
        profile.setPassword(PasswordHasher.hashPwd("oldpass"));

        when(dao.findById(profileId)).thenReturn(Optional.of(profile));

        BadRequestException ex = assertThrows(
                BadRequestException.class,
                () -> service.changePassword(profileId, dto)
        );

        assertEquals("error.password.required", ex.getMessage());
        verify(dao).findById(profileId);
        verify(dao, never()).update(any());
    }

    @Test
    void changePassword_shouldThrowBadRequestException_whenConfirmPasswordDoesNotMatch() {
        long profileId = 1L;

        PasswordChangeDto dto = new PasswordChangeDto();
        dto.setVersion(1);
        dto.setCurrentPassword("oldpass");
        dto.setNewPassword("newpass");
        dto.setConfirmPassword("otherpass");

        Profile existing = new Profile();
        existing.setId(profileId);
        existing.setPassword(PasswordHasher.hashPwd("oldpass"));

        when(dao.findById(profileId)).thenReturn(Optional.of(existing));

        BadRequestException ex = assertThrows(
                BadRequestException.class,
                () -> service.changePassword(profileId, dto)
        );

        assertEquals("error.password.mismatch", ex.getMessage());
        verify(dao).findById(profileId);
        verify(dao, never()).update(any());
    }

    @Test
    void changePassword_shouldThrowBadRequestException_whenCurrentPasswordIsIncorrect() {
        long profileId = 1L;

        PasswordChangeDto dto = new PasswordChangeDto();
        dto.setVersion(1);
        dto.setCurrentPassword("wrongpass");
        dto.setNewPassword("newpass");
        dto.setConfirmPassword("newpass");

        Profile profile = new Profile();
        profile.setId(profileId);
        profile.setPassword(PasswordHasher.hashPwd("oldpass"));

        when(dao.findById(profileId)).thenReturn(Optional.of(profile));

        BadRequestException ex = assertThrows(
                BadRequestException.class,
                () -> service.changePassword(profileId, dto)
        );

        assertEquals("error.password.invalid-current", ex.getMessage());
        verify(dao).findById(profileId);
        verify(dao, never()).update(any());
    }

    @Test
    void changePassword_shouldThrowBadRequestException_whenNewPasswordMatchesCurrentPassword() {
        long profileId = 1L;

        PasswordChangeDto dto = new PasswordChangeDto();
        dto.setVersion(1);
        dto.setCurrentPassword("samepass");
        dto.setNewPassword("samepass");
        dto.setConfirmPassword("samepass");

        Profile profile = new Profile();
        profile.setId(profileId);
        profile.setPassword(PasswordHasher.hashPwd("samepass"));

        when(dao.findById(profileId)).thenReturn(Optional.of(profile));

        BadRequestException ex = assertThrows(
                BadRequestException.class,
                () -> service.changePassword(profileId, dto)
        );

        assertEquals("error.password.same-as-current", ex.getMessage());
        verify(dao).findById(profileId);
        verify(dao, never()).update(any());
    }
}
