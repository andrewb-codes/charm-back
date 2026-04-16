package ru.andrewb.charm.back.service.command;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.FieldDefaults;
import ru.andrewb.charm.back.model.Gender;

import java.time.LocalDate;

@Getter
@Setter
@ToString(onlyExplicitlyIncluded = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProfileUpdateCommand {

    @ToString.Include
    String name;

    @ToString.Include
    String surname;

    String about;
    LocalDate birthdate;
    Gender gender;
    Integer version;
}
