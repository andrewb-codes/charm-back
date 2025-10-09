package ru.andrewb.charm.back.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString(onlyExplicitlyIncluded = true)
public class EmailChangeDto {
    @ToString.Include
    private String newEmail;

    private String currentPassword;
}
