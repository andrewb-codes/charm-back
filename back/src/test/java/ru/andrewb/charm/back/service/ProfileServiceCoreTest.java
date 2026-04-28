package ru.andrewb.charm.back.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.multipart.MultipartFile;
import ru.andrewb.charm.back.dao.ProfileDao;
import ru.andrewb.charm.back.mapper.ProfileToProfileGetDtoMapper;
import ru.andrewb.charm.back.mapper.ProfileUpdateCommandToProfileMapper;
import ru.andrewb.charm.back.model.Profile;
import ru.andrewb.charm.back.model.exception.BadRequestException;
import ru.andrewb.charm.back.model.exception.DuplicateEmailException;
import ru.andrewb.charm.back.model.exception.NotFoundException;
import ru.andrewb.charm.back.service.command.ProfileUpdateCommand;
import ru.andrewb.charm.back.service.command.ProfileUpdateStatusCommand;
import ru.andrewb.charm.back.service.command.RegistrationCommand;

import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProfileServiceCoreTest {

    @Mock
    private ProfileDao dao;

    @Mock
    private ContentService contentService;

    @Mock
    private ProfileToProfileGetDtoMapper profileToProfileGetDtoMapper;

    @Mock
    private ProfileUpdateCommandToProfileMapper profileUpdateCommandToProfileMapper;

    @Mock
    private MultipartFile photo;

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
    void save_shouldPersistNormalizedEmailAndEncodedPassword() {
        RegistrationCommand command = new RegistrationCommand();
        command.setEmail("  user@mail.com  ");
        command.setPassword("123456");

        when(dao.existsEmail("user@mail.com", null)).thenReturn(false);
        when(dao.save(any(Profile.class))).thenAnswer(inv -> {
            Profile p = inv.getArgument(0);
            p.setId(42L);
            return p;
        });

        Long id = service.save(command);

        assertEquals(42L, id);
        ArgumentCaptor<Profile> captor = ArgumentCaptor.forClass(Profile.class);
        verify(dao).save(captor.capture());
        Profile saved = captor.getValue();
        assertEquals("user@mail.com", saved.getEmail());
        assertNotNull(saved.getPassword());
        assertNotEquals("123456", saved.getPassword());
        assertTrue(passwordEncoder.matches("123456", saved.getPassword()));
    }

    @Test
    void save_shouldThrowDuplicateEmailException_whenEmailAlreadyExists() {
        RegistrationCommand command = new RegistrationCommand();
        command.setEmail("user@mail.com");
        command.setPassword("123456");
        when(dao.existsEmail("user@mail.com", null)).thenReturn(true);

        DuplicateEmailException ex = assertThrows(
                DuplicateEmailException.class,
                () -> service.save(command)
        );

        assertEquals("error.email.exists", ex.getMessage());
        verify(dao, never()).save(any());
    }

    @Test
    void update_shouldThrowBadRequestException_whenVersionIsMissing() {
        ProfileUpdateCommand command = new ProfileUpdateCommand();
        command.setVersion(null);
        when(dao.findById(1L)).thenReturn(Optional.of(new Profile()));

        BadRequestException ex = assertThrows(
                BadRequestException.class,
                () -> service.update(1L, command, null)
        );

        assertEquals("error.param.required", ex.getMessage());
        verify(dao, never()).update(any());
    }

    @Test
    void update_shouldReplacePhotoAndPersistProfile_whenPhotoProvided() throws Exception {
        Profile existing = new Profile();
        existing.setId(5L);
        existing.setPhoto("old.png");

        Profile mapped = new Profile();
        mapped.setId(5L);

        ProfileUpdateCommand command = new ProfileUpdateCommand();
        command.setVersion(7);

        when(dao.findById(5L)).thenReturn(Optional.of(existing));
        when(profileUpdateCommandToProfileMapper.map(command, existing)).thenReturn(mapped);
        when(photo.isEmpty()).thenReturn(false);
        when(photo.getOriginalFilename()).thenReturn("C:/tmp/new.png");
        when(photo.getInputStream()).thenReturn(new ByteArrayInputStream("x".getBytes()));

        service.update(5L, command, photo);

        verify(contentService).delete("profile", "5", "old.png");
        verify(contentService).upload(any(), eq("profile"), eq("5"), eq("new.png"));
        assertEquals("new.png", mapped.getPhoto());
        verify(dao).update(mapped);
    }

    @Test
    void updateStatuses_shouldSkipDaoCall_whenListIsEmpty() {
        service.updateStatuses(List.of());
        verify(dao, never()).updateStatuses(anyList());
    }

    @Test
    void updateStatuses_shouldDelegateToDao_whenListIsNotEmpty() {
        ProfileUpdateStatusCommand command = new ProfileUpdateStatusCommand();
        command.setId(1L);

        service.updateStatuses(List.of(command));

        verify(dao).updateStatuses(anyList());
    }

    @Test
    void delete_shouldReturnFalse_whenIdIsNull() {
        assertFalse(service.delete(null));
        verifyNoInteractions(contentService);
        verify(dao, never()).delete(anyLong());
    }

    @Test
    void delete_shouldThrowNotFound_whenDaoDeleteReturnsFalse() {
        when(dao.delete(7L)).thenReturn(false);

        NotFoundException ex = assertThrows(
                NotFoundException.class,
                () -> service.delete(7L)
        );

        assertEquals("error.profile.not-found", ex.getMessage());
        verify(contentService).deleteTree("profile", "7");
        verify(dao).delete(7L);
    }
}
