package ru.andrewb.charm.back.dto;

import jakarta.servlet.http.Part;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import ru.andrewb.charm.back.model.Gender;
import ru.andrewb.charm.back.model.Status;

import java.time.LocalDate;

@Getter
@Setter
@ToString(onlyExplicitlyIncluded = true)
public class ProfileUpdateDto {
    @ToString.Include
    private String name;
    @ToString.Include
    private String surname;

    private String about;
    private LocalDate birthDate;
    private Gender gender;
    private Status status;
    private Part photo;
}