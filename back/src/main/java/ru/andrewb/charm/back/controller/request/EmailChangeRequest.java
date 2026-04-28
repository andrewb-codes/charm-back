package ru.andrewb.charm.back.controller.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@ToString(onlyExplicitlyIncluded = true)
@Schema(description = "Email change request")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class EmailChangeRequest {

    @ToString.Include
    @Schema(description = "New email", example = "new-email@mail.com")
    @NotBlank(message = "error.email.required")
    @Email(message = "error.email.invalid")
    String newEmail;

    @Schema(description = "Current password", example = "123456", accessMode = Schema.AccessMode.WRITE_ONLY)
    @NotBlank(message = "error.password.current-required")
    String currentPassword;

    @Schema(description = "Optimistic locking version", example = "0")
    @NotNull(message = "error.param.required")
    Integer version;
}
