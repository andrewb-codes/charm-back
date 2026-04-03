package ru.andrewb.charm.back.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.andrewb.charm.back.dao.ProfileDao;
import ru.andrewb.charm.back.dto.LoginDto;
import ru.andrewb.charm.back.dto.UserDetailsDto;
import ru.andrewb.charm.back.mapper.ProfileToProfileGetDtoMapper;
import ru.andrewb.charm.back.mapper.ProfileToUserDetailsDtoMapper;
import ru.andrewb.charm.back.mapper.ProfileUpdateDtoToProfileMapper;
import ru.andrewb.charm.back.model.Profile;
import ru.andrewb.charm.back.model.Role;
import ru.andrewb.charm.back.model.exception.BadRequestException;
import ru.andrewb.charm.back.security.PasswordHasher;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProfileServiceLoginTest {

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
    void login_shouldReturnUserDetails_whenCredentialsAreCorrect() {
        LoginDto dto = new LoginDto();
        dto.setEmail("user@mail.com");
        dto.setPassword("123456");

        Profile profile = new Profile();
        profile.setId(1L);
        profile.setEmail("user@mail.com");
        profile.setPassword(PasswordHasher.hashPwd("123456"));
        profile.setRole(Role.USER);

        UserDetailsDto expected = new UserDetailsDto();
        expected.setId(1L);
        expected.setEmail("user@mail.com");
        expected.setRole(Role.USER);

        when(dao.findByEmail("user@mail.com")).thenReturn(Optional.of(profile));
        when(profileToUserDetailsDtoMapper.map(profile)).thenReturn(expected);

        UserDetailsDto result = service.login(dto);

        assertSame(expected, result);
        verify(dao).findByEmail("user@mail.com");
        verify(profileToUserDetailsDtoMapper).map(profile);
    }

    @Test
    void login_shouldThrowBadRequestException_whenUserIsNotFound() {
        LoginDto dto = new LoginDto();
        dto.setEmail("missing@mail.com");
        dto.setPassword("123456");

        when(dao.findByEmail("missing@mail.com")).thenReturn(Optional.empty());

        BadRequestException ex = assertThrows(
                BadRequestException.class,
                () -> service.login(dto)
        );

        assertEquals("error.login.bad-credentials",  ex.getMessage());
        verify(dao).findByEmail("missing@mail.com");
    }

    @Test
    void login_shouldThrowBadRequestException_whenPasswordIsIncorrect() {
        LoginDto dto = new LoginDto();
        dto.setEmail("user@mail.com");
        dto.setPassword("wrong-password");

        Profile profile = new Profile();
        profile.setEmail("user@mail.com");
        profile.setPassword(PasswordHasher.hashPwd("correct-password"));

        when(dao.findByEmail("user@mail.com")).thenReturn(Optional.of(profile));

        BadRequestException ex = assertThrows(
                BadRequestException.class,
                () -> service.login(dto)
        );

        assertEquals("error.login.bad-credentials",  ex.getMessage());
        verify(dao).findByEmail("user@mail.com");
    }

    @Test
    void login_shouldTrimEmail_beforeSearchingUser() {
        LoginDto dto = new LoginDto();
        dto.setEmail("   user@mail.com   ");
        dto.setPassword("123456");

        Profile profile = new Profile();
        profile.setId(1L);
        profile.setEmail("user@mail.com");
        profile.setPassword(PasswordHasher.hashPwd("123456"));

        UserDetailsDto expected = new UserDetailsDto();
        expected.setId(1L);
        expected.setEmail("user@mail.com");

        when(dao.findByEmail("user@mail.com")).thenReturn(Optional.of(profile));
        when(profileToUserDetailsDtoMapper.map(profile)).thenReturn(expected);

        UserDetailsDto result = service.login(dto);

        assertSame(expected, result);
        verify(dao).findByEmail("user@mail.com");
    }
}
