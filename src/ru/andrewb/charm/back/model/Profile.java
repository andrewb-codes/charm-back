package ru.andrewb.charm.back.model;

import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;

@Data
@ToString(onlyExplicitlyIncluded = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Profile {
    @EqualsAndHashCode.Include
    Long id;

    @ToString.Include
    String email;
    @ToString.Include
    String name;
    @ToString.Include
    String surname;

    String password;
    String about;
    LocalDate birthdate;
    Gender gender;
    Status status;
    String photo;
    Role role;
    Integer version;
}
