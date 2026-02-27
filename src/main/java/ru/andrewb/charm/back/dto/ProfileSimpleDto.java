package ru.andrewb.charm.back.dto;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@ToString(onlyExplicitlyIncluded = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProfileSimpleDto {
    @ToString.Include
    Long id;
    @ToString.Include
    String name;
    @ToString.Include
    String surname;
    @ToString.Include
    Integer age;

    String about;
    String photo;
}
