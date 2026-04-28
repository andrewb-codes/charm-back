package ru.andrewb.charm.back.controller.request;

import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "Admin profile search filters")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProfilesFilterRequest {

    @Schema(description = "Email prefix", example = "ivan")
    String emailStartsWith;

    @Schema(description = "Name prefix", example = "Iv")
    String nameStartsWith;

    @Schema(description = "Surname prefix", example = "Iv")
    String surnameStartsWith;

    @Schema(description = "Upper age bound, exclusive", example = "35")
    @Min(value = 0, message = "error.param.invalid")
    Integer ltAge;

    @Schema(description = "Lower age bound, inclusive", example = "18")
    @Min(value = 0, message = "error.param.invalid")
    Integer gteAge;

    @Schema(description = "Profile role", example = "USER")
    Role role;

    @Schema(description = "Profile status", example = "ACTIVE")
    Status status;

    @Schema(description = "Sort field", example = "ID")
    SortBy sortBy;

    @Schema(description = "Sort order", example = "ASC")
    SortOrder sortOrder;

    @Schema(description = "Page number, starting from 1", example = "1")
    @Min(value = 1, message = "error.param.invalid")
    Integer page;

    @Schema(description = "Items per page", example = "20")
    @Min(value = 1, message = "error.param.invalid")
    Integer pageSize;
}
