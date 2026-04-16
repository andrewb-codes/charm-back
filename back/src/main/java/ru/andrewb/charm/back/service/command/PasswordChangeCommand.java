package ru.andrewb.charm.back.service.command;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PasswordChangeCommand {

    String currentPassword;
    String newPassword;
    String confirmPassword;
    Integer version;
}
