package ru.andrewb.charm.back.dto;

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
public class ProfileUpdateDto {
    @ToString.Include
    String name;
    @ToString.Include
    String surname;

    String about;
    LocalDate birthdate;
    Gender gender;
    Integer version;
}