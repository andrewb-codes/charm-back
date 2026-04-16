package ru.andrewb.charm.back.controller.request;

import jakarta.validation.constraints.Min;
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
public class ProfilesFilterRequest {

    String emailStartsWith;
    String nameStartsWith;
    String surnameStartsWith;

    @Min(value = 0, message = "error.param.invalid")
    Integer ltAge;

    @Min(value = 0, message = "error.param.invalid")
    Integer gteAge;

    Role role;
    Status status;
    SortBy sortBy;
    SortOrder sortOrder;

    @Min(value = 1, message = "error.param.invalid")
    Integer page;

    @Min(value = 1, message = "error.param.invalid")
    Integer pageSize;
}
