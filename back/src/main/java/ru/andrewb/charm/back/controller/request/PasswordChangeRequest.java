package ru.andrewb.charm.back.controller.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PasswordChangeRequest {

    @NotBlank(message = "error.password.current-required")
    String currentPassword;

    @NotBlank(message = "error.password.new-required")
    @Size(min = 6, message = "error.password.short")
    String newPassword;

    @NotBlank(message = "error.password.confirm-required")
    String confirmPassword;

    @NotNull(message = "error.param.required")
    Integer version;
}
