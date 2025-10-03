package ru.andrewb.charm.back.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDate;

@Data
@ToString(onlyExplicitlyIncluded = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Profile {
    @EqualsAndHashCode.Include
    private Long id;

    @ToString.Include
    private String email;
    @ToString.Include
    private String name;
    @ToString.Include
    private String surname;

    private String password;
    private String about;
    private LocalDate birthDate;
    private Gender gender;
    private Status status;
}
