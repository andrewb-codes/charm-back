package ru.andrewb.charm.back.service.command;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@ToString(onlyExplicitlyIncluded = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class EmailChangeCommand {

    @ToString.Include
    String newEmail;

    String currentPassword;
    Integer version;
}
