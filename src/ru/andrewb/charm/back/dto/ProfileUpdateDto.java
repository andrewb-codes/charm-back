package ru.andrewb.charm.back.dto;

import jakarta.servlet.http.Part;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.FieldDefaults;
import ru.andrewb.charm.back.model.Gender;
import ru.andrewb.charm.back.model.Status;

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
    Status status;
    Part photo;
    Integer version;
}