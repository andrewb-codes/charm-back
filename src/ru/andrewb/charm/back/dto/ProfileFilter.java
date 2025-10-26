package ru.andrewb.charm.back.dto;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import ru.andrewb.charm.back.model.Status;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProfileFilter {
    String emailStartsWith;
    String nameStartsWith;
    String surnameStartsWith;
    Integer lowerAgeBound;
    Integer greaterAndEqualAgeBound;
    Status status;
}
