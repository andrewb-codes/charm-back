package ru.andrewb.charm.back.dto;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PasswordChangeDto {
    String currentPassword;
    String newPassword;
    String confirmPassword;
    Integer version;
}
