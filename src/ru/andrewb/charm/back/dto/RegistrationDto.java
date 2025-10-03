package ru.andrewb.charm.back.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString(onlyExplicitlyIncluded = true)
public class RegistrationDto {
    @ToString.Include
    private String email;

    private String password;
}
