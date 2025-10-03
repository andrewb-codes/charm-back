package ru.andrewb.charm.back.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import ru.andrewb.charm.back.model.Gender;
import ru.andrewb.charm.back.model.Status;

import java.time.LocalDate;

@Getter
@Setter
@ToString(onlyExplicitlyIncluded = true)
public class ProfileGetDto {
    @ToString.Include
    private Long id;
    @ToString.Include
    private String email;
    @ToString.Include
    private String name;
    @ToString.Include
    private String surname;
    @ToString.Include
    private Integer age;

    private String about;
    private LocalDate birthDate;
    private Gender gender;
    private Status status;
}
