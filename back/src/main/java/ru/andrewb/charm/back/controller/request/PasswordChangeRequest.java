package ru.andrewb.charm.back.controller.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Schema(description = "Password change request")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PasswordChangeRequest {

    @Schema(description = "Current password", example = "123456", accessMode = Schema.AccessMode.WRITE_ONLY)
    @NotBlank(message = "error.password.current-required")
    String currentPassword;

    @Schema(description = "New password, at least 6 characters", example = "654321", accessMode = Schema.AccessMode.WRITE_ONLY)
    @NotBlank(message = "error.password.new-required")
    @Size(min = 6, message = "error.password.short")
    String newPassword;

    @Schema(description = "New password confirmation", example = "654321", accessMode = Schema.AccessMode.WRITE_ONLY)
    @NotBlank(message = "error.password.confirm-required")
    String confirmPassword;

    @Schema(description = "Optimistic locking version", example = "0")
    @NotNull(message = "error.param.required")
    Integer version;
}
