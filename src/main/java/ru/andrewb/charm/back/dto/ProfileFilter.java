package ru.andrewb.charm.back.dto;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import ru.andrewb.charm.back.dto.sort.SortBy;
import ru.andrewb.charm.back.dto.sort.SortOrder;
import ru.andrewb.charm.back.model.Role;
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
    Role role;
    Status status;

    SortBy sortBy;
    SortOrder sortOrder;

    Integer page;
    Integer pageSize;
}
