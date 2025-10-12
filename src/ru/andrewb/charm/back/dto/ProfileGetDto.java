package ru.andrewb.charm.back.dto;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.FieldDefaults;
import ru.andrewb.charm.back.model.Gender;
import ru.andrewb.charm.back.model.Role;
import ru.andrewb.charm.back.model.Status;

import java.time.LocalDate;

@Getter
@Setter
@ToString(onlyExplicitlyIncluded = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProfileGetDto {
    @ToString.Include
    Long id;
    @ToString.Include
    String email;
    @ToString.Include
    String name;
    @ToString.Include
    String surname;
    @ToString.Include
    Integer age;

    String about;
    LocalDate birthDate;
    Gender gender;
    Status status;
    String photo;
    Role role;
}
