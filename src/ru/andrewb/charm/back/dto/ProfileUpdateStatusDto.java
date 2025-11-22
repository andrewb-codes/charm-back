package ru.andrewb.charm.back.dto;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.FieldDefaults;
import ru.andrewb.charm.back.model.Status;

@Getter
@Setter
@ToString(onlyExplicitlyIncluded = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProfileUpdateStatusDto {
    @ToString.Include
    Long id;
    @ToString.Include
    Status status;
}
