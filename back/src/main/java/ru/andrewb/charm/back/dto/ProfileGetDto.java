package ru.andrewb.charm.back.dto;

import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "Full profile DTO")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProfileGetDto {
    @ToString.Include
    @Schema(description = "Profile id", example = "1")
    Long id;

    @ToString.Include
    @Schema(description = "Profile email", example = "ivanov@mail.ru")
    String email;

    @ToString.Include
    @Schema(description = "Name", example = "Ivan")
    String name;

    @ToString.Include
    @Schema(description = "Surname", example = "Ivanov")
    String surname;

    @ToString.Include
    @Schema(description = "Calculated age", example = "24")
    Integer age;

    @Schema(description = "Profile description", example = "I am QA")
    String about;

    @Schema(description = "Birthdate", example = "2001-12-03")
    LocalDate birthdate;

    @Schema(description = "Gender", example = "MALE")
    Gender gender;

    @Schema(description = "Profile status", example = "ACTIVE")
    Status status;

    @Schema(description = "Stored profile photo filename", example = "avatar.jpg")
    String photo;

    @Schema(description = "Profile role", example = "USER")
    Role role;

    @Schema(description = "Optimistic locking version", example = "0")
    Integer version;
}
